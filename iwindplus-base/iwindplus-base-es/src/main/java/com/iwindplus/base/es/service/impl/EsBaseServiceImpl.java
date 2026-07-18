/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.es.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.es.domain.EsDbBaseDO;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.domain.property.EsProperty;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import jakarta.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.MultiGetItem;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

/**
 * es业务层基础接口实现类.
 *
 * @author zengdegui
 * @since 2026/04/12 15:15
 */
@Slf4j
@Getter
public class EsBaseServiceImpl<T extends EsDbBaseDO> implements EsBaseService<T> {

    /**
     * 反射实体.
     */
    private final Class<T> entityClass;

    @Resource
    private ElasticsearchOperations operations;

    @Resource
    private ElasticsearchTemplate template;

    @Resource
    private EsProperty property;

    /**
     * 构造方法.
     */
    @SuppressWarnings("unchecked")
    public EsBaseServiceImpl() {
        this.entityClass = (Class<T>) ReflectionKit.getSuperClassGenericType(
            this.getClass(), EsBaseServiceImpl.class, 0);
    }

    @Override
    public T save(T entity) {
        this.buildDefaultEntity(entity, getCurrentUserInfo());
        return operations.save(entity);
    }

    @Override
    public Collection<T> saveBatch(Collection<T> entities, int batchSize) {
        if (CollUtil.isEmpty(entities)) {
            return CollUtil.newArrayList();
        }

        final UserBaseVO userInfo = getCurrentUserInfo();

        entities.forEach(entity -> this.buildDefaultEntity(entity, userInfo));

        batchExecute(entities, batchSize, this::bulkIndex);

        return entities;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<T> entities, int batchSize) {
        if (CollUtil.isEmpty(entities)) {
            return false;
        }

        List<T> saveList = new ArrayList<>();
        List<T> updateList = new ArrayList<>();

        for (T entity : entities) {
            if (CharSequenceUtil.isBlank(entity.getId())) {
                saveList.add(entity);
            } else {
                updateList.add(entity);
            }
        }

        boolean saveResult = CollUtil.isNotEmpty(saveList)
            && CollUtil.isNotEmpty(saveBatch(saveList, batchSize));

        boolean updateResult = CollUtil.isNotEmpty(updateList)
            && updateBatchById(updateList, batchSize);

        return saveResult || updateResult;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> ids, boolean deleted) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        if (Boolean.TRUE.equals(deleted)) {
            ids.forEach(id -> operations.delete(id.toString(), entityClass));
            return true;
        }

        // 逻辑删除
        List<UpdateQuery> queries = ids.stream().map(id -> {
            Document doc = Document.create();
            doc.put(DbConstant.DELETED, NumberConstant.NUMBER_ONE);

            return UpdateQuery.builder(id.toString())
                .withDocument(doc)
                .build();
        }).toList();

        operations.bulkUpdate(queries, entityClass);
        return true;
    }

    @Override
    public boolean remove(EsLambdaQueryWrapper<T> wrapper) {
        DeleteQuery deleteQuery = DeleteQuery.builder(wrapper.build()).build();

        ByQueryResponse resp = operations.delete(deleteQuery, entityClass);

        return resp.getDeleted() > 0;
    }

    @Override
    public boolean updateById(T entity) {
        if (entity == null || CharSequenceUtil.isBlank(entity.getId())) {
            return false;
        }

        fillAuditFields(entity, false, getCurrentUserInfo());

        operations.update(entity);

        return true;
    }

    @Override
    public boolean updateBatchById(Collection<T> entities, int batchSize) {
        if (CollUtil.isEmpty(entities)) {
            return false;
        }

        UserBaseVO user = getCurrentUserInfo();

        for (T entity : entities) {
            fillAuditFields(entity, false, user);
        }

        batchExecute(entities, batchSize, sub -> {
            bulkIndex(sub);
            return true;
        });

        return true;
    }

    @Override
    public <E extends IPage<T>> E page(E page, EsLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        SearchHits<T> hits = operations.search(wrapper.build(page), entityClass);

        List<T> list = toEntityList(hits);

        page.setRecords(list);
        page.setTotal(hits.getTotalHits());

        return page;
    }

    @Override
    public EsPageDTO<T> pageByAfter(EsPageDTO<T> page, EsLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        wrapper.orderByDesc(T::getModifiedTimestamp)
            .orderByDesc(T::getId);

        // 注入游标
        wrapper.searchAfter(page.getSearchAfter());

        SearchHits<T> hits = operations.search(
            wrapper.buildSearchAfter(page.getSize()),
            entityClass
        );

        List<T> list = toEntityList(hits);

        page.setRecords(list);
        page.setTotal(hits.getTotalHits());

        if (!hits.isEmpty()) {
            SearchHit<T> last = hits.getSearchHits()
                .get(hits.getSearchHits().size() - 1);

            page.setSearchAfter(last.getSortValues());
        }

        return page;
    }

    @Override
    public List<T> list(EsLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        SearchHits<T> hits = operations.search(wrapper.build(), entityClass);

        return toEntityList(hits);
    }

    @Override
    public T getById(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return null;
        }

        return operations.get(id, entityClass);
    }

    @Override
    public List<T> listById(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        Query query = NativeQuery.builder()
            .withQuery(q -> q.ids(i -> i.values(ids)))
            .build();

        List<MultiGetItem<T>> items = operations.multiGet(query, entityClass);

        return items.stream()
            .map(MultiGetItem::getItem)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public T getOne(EsLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        SearchHits<T> hits = operations.search(
            wrapper.build(PageRequest.of(0, 1)),
            entityClass
        );

        return hits.getSearchHits().isEmpty()
            ? null
            : hits.getSearchHits().get(0).getContent();
    }

    @Override
    public long count(EsLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        return operations.count(wrapper.build(), entityClass);
    }

    @Override
    public boolean exists(EsLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        return operations.count(wrapper.build(PageRequest.of(0, 1)), entityClass) > 0;
    }

    private boolean bulkIndex(Collection<T> list) {
        List<IndexQuery> queries = list.stream().map(e -> {
            IndexQuery q = new IndexQuery();
            q.setObject(e);
            return q;
        }).toList();

        operations.bulkIndex(queries, entityClass);

        return true;
    }

    private void buildDefaultEntity(T entity, UserBaseVO userInfo) {
        entity.setId(null);
        fillAuditFields(entity, true, userInfo);
    }

    private void fillAuditFields(T entity, boolean isInsert, UserBaseVO userInfo) {
        final int numberZero = NumberConstant.NUMBER_ZERO;
        final Long userId = userInfo.getUserId();
        final String realName = userInfo.getRealName();

        if (isInsert) {
            if (Boolean.FALSE.equals(this.property.getField().getFill().getEnabled())) {
                return;
            }

            if (Boolean.TRUE.equals(this.property.getField().getFill().getEnabledInsertStrict())) {
                this.fillStrictInsert(entity, userId, realName);
            } else {
                this.fillOptionalInsert(entity, userId, realName);
            }
            entity.setDeleted(numberZero);
            entity.setVersion(numberZero);
        } else {
            this.fillUpdate(entity, userId, realName);
        }
    }

    private void fillStrictInsert(T entity, Long userId, String realName) {
        final LocalDateTime now = LocalDateTime.now();
        final long currentTimeMillis = System.currentTimeMillis();

        entity.setCreatedTime(now);
        entity.setCreatedTimestamp(currentTimeMillis);
        entity.setCreatedBy(realName);
        entity.setCreatedId(userId);
        entity.setModifiedTime(now);
        entity.setModifiedTimestamp(currentTimeMillis);
        entity.setModifiedBy(realName);
        entity.setModifiedId(userId);
    }

    private void fillOptionalInsert(T entity, Long userId, String realName) {
        final LocalDateTime now = LocalDateTime.now();
        final long currentTimeMillis = System.currentTimeMillis();

        if (entity.getCreatedTime() == null) {
            entity.setCreatedTime(now);
        }
        if (entity.getCreatedTimestamp() == null) {
            entity.setCreatedTimestamp(currentTimeMillis);
        }
        if (CharSequenceUtil.isBlank(entity.getCreatedBy())) {
            entity.setCreatedBy(realName);
        }
        if (entity.getCreatedId() == null) {
            entity.setCreatedId(userId);
        }

        if (entity.getModifiedTime() == null) {
            entity.setModifiedTime(now);
        }
        if (entity.getModifiedTimestamp() == null) {
            entity.setModifiedTimestamp(currentTimeMillis);
        }
        if (CharSequenceUtil.isBlank(entity.getModifiedBy())) {
            entity.setModifiedBy(realName);
        }
        if (entity.getModifiedId() == null) {
            entity.setModifiedId(userId);
        }
    }

    private void fillUpdate(T entity, Long userId, String realName) {
        final LocalDateTime now = LocalDateTime.now();
        final long currentTimeMillis = System.currentTimeMillis();

        entity.setModifiedTime(now);
        entity.setModifiedTimestamp(currentTimeMillis);
        entity.setModifiedBy(realName);
        entity.setModifiedId(userId);

        if (entity.getVersion() != null) {
            entity.setVersion(entity.getVersion() + 1);
        }
    }

    private <E> boolean batchExecute(
        Collection<E> list,
        int batchSize,
        Function<List<E>, Boolean> executor) {

        if (CollUtil.isEmpty(list)) {
            return false;
        }

        if (list.size() <= batchSize) {
            return executor.apply(new ArrayList<>(list));
        }

        boolean result = true;

        for (List<E> sub : CollUtil.split(list, batchSize)) {
            if (!executor.apply(sub)) {
                result = false;
            }
        }

        return result;
    }

    private List<T> toEntityList(SearchHits<T> hits) {
        return hits.getSearchHits().stream().map(hit -> {

            T entity = hit.getContent();

            return entity;
        }).toList();
    }

    private EsLambdaQueryWrapper wrapQueryByDelete(EsLambdaQueryWrapper<T> wrapper) {
        wrapper.eq(DbConstant.DELETED, NumberConstant.NUMBER_ZERO);
        return wrapper;
    }

    private UserBaseVO getCurrentUserInfo() {
        return Optional.ofNullable(UserContextHolder.getContext())
            .orElse(UserContextHolder.getDefaultUser());
    }
}