/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.mongo.domain.MongoDbBaseDO;
import com.iwindplus.base.mongo.domain.property.MongoProperty;
import com.iwindplus.base.mongo.service.MongoBaseService;
import com.iwindplus.base.mongo.support.MongoLambdaQueryWrapper;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.ReflectionUtils;

/**
 * mongo业务层基础接口实现类.
 *
 * @param <T> 对象
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
@Getter
public class MongoBaseServiceImpl<T extends MongoDbBaseDO> implements MongoBaseService<T> {

    /**
     * 反射实体.
     */
    private final Class<T> entityClass;

    /**
     * MongoTemplate.
     */
    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private MongoProperty property;

    /**
     * 构造方法.
     */
    @SuppressWarnings("unchecked")
    public MongoBaseServiceImpl() {
        this.entityClass = (Class<T>) ReflectionKit.getSuperClassGenericType(
            this.getClass(), MongoBaseServiceImpl.class, 0);
    }

    @Override
    public T save(T entity) {
        this.buildDefaultEntity(entity, getCurrentUserInfo());
        return this.mongoTemplate.save(entity);
    }

    @Override
    public Collection<T> saveBatch(Collection<T> entities, int batchSize) {
        if (CollUtil.isEmpty(entities)) {
            return CollUtil.newArrayList();
        }

        final UserBaseVO userInfo = getCurrentUserInfo();

        entities.forEach(entity -> this.buildDefaultEntity(entity, userInfo));

        if (entities.size() <= batchSize) {
            return this.mongoTemplate.insertAll(entities);
        }

        List<T> result = new ArrayList<>(entities.size());

        batchExecute(entities, batchSize, sub -> {
            result.addAll(this.mongoTemplate.insertAll(sub));
            return true;
        });

        return result;
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
            && CollUtil.isNotEmpty(this.saveBatch(saveList, batchSize));

        boolean updateResult = CollUtil.isNotEmpty(updateList)
            && this.updateBatchById(updateList, batchSize);

        return saveResult || updateResult;
    }

    @Override
    public boolean removeByIds(Collection<?> ids, boolean deleted) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        Query query = Query.query(Criteria.where(DbConstant.ID).in(ids));
        return this.remove(query, deleted);
    }

    @Override
    public boolean remove(Query query, boolean deleted) {
        if (Boolean.TRUE.equals(deleted)) {
            DeleteResult result = this.mongoTemplate.remove(query, this.entityClass);
            return result != null && result.getDeletedCount() > 0;
        }

        query.addCriteria(this.getCriteriaDeleted());
        Update update = this.getUpdateDeleted();

        UpdateResult result = this.mongoTemplate.updateMulti(query, update, this.entityClass);
        return result != null && result.getModifiedCount() > 0;
    }

    @Override
    public boolean updateById(T entity) {
        if (Objects.isNull(entity)) {
            return false;
        }
        Query query = Query.query(Criteria.where(DbConstant.ID).is(entity.getId()));
        return this.update(entity, query);
    }

    @Override
    public boolean update(T entity, Query query) {
        query.addCriteria(this.getCriteriaDeleted());

        Update update = this.getUpdate(entity);
        UpdateResult result = this.mongoTemplate.updateMulti(query, update, this.entityClass);

        return result != null && result.getModifiedCount() > 0;
    }

    @Override
    public boolean updateBatchById(Collection<T> entities, int batchSize) {
        return batchExecute(entities, batchSize, this::updateBatchByIds);
    }

    @Override
    public <E extends IPage<T>> E page(E page, Query query) {
        List<Sort.Order> orders = new ArrayList<>();
        List<OrderItem> orderList = page.orders();

        if (CollUtil.isEmpty(orderList)) {
            orders.add(Sort.Order.desc(DbConstant.MODIFIED_TIME));
        } else {
            orderList.forEach(order ->
                orders.add(order.isAsc()
                    ? Sort.Order.asc(order.getColumn())
                    : Sort.Order.desc(order.getColumn()))
            );
        }

        query.addCriteria(this.getCriteriaDeleted());

        long count = this.mongoTemplate.count(query, this.entityClass);
        if (count == 0) {
            return page;
        }

        PageRequest pageRequest = PageRequest.of(
            (int) page.getCurrent() - 1,
            (int) page.getSize()
        );

        List<T> list = this.mongoTemplate.find(
            query.with(pageRequest).with(Sort.by(orders)),
            this.entityClass
        );

        page.setRecords(list);
        page.setTotal(count);
        return page;
    }

    @Override
    public List<T> list() {
        return this.mongoTemplate.find(Query.query(this.getCriteriaDeleted()), this.entityClass);
    }

    @Override
    public List<T> listById(List<String> ids) {
        return this.mongoTemplate.find(Query.query(Criteria.where(DbConstant.ID).in(ids)),
            this.entityClass);
    }

    @Override
    public List<T> list(Query query) {
        query.addCriteria(this.getCriteriaDeleted());
        return this.mongoTemplate.find(query, this.entityClass);
    }

    @Override
    public T getById(String id) {
        if (CharSequenceUtil.isBlank(id)) {
            return null;
        }
        return this.mongoTemplate.findById(id, this.entityClass);
    }

    @Override
    public T getOne(Query query) {
        query.addCriteria(this.getCriteriaDeleted());
        return this.mongoTemplate.findOne(query, this.entityClass);
    }

    @Override
    public long count(Query query) {
        query.addCriteria(this.getCriteriaDeleted());
        return this.mongoTemplate.count(query, this.entityClass);
    }

    @Override
    public boolean exists(MongoLambdaQueryWrapper<T> wrapper) {
        wrapQueryByDelete(wrapper);
        return this.mongoTemplate.exists(wrapper.build(), this.entityClass);
    }

    /**
     * 批量更新（Bulk）
     */
    private boolean updateBatchByIds(Collection<T> entities) {
        BulkOperations operations = this.mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED, this.entityClass);

        for (T entity : entities) {
            operations.updateMulti(this.getQuery(entity), this.getUpdate(entity));
        }

        BulkWriteResult result = operations.execute();
        return result != null && result.getModifiedCount() > 0;
    }

    /**
     * 通用批处理执行器
     */
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
    }

    private Query getQuery(T entity) {
        Criteria criteria = Criteria.where(DbConstant.ID).is(entity.getId())
            .andOperator(this.getCriteriaDeleted());

        if (entity.getVersion() != null) {
            criteria.and(DbConstant.VERSION).is(entity.getVersion());
        }

        return Query.query(criteria);
    }

    private Update getUpdateDeleted() {
        UserBaseVO user = getCurrentUserInfo();

        return Update.update(DbConstant.DELETED, NumberConstant.NUMBER_ONE)
            .set(DbConstant.MODIFIED_TIME, LocalDateTime.now())
            .set(DbConstant.MODIFIED_TIMESTAMP, System.currentTimeMillis())
            .set(DbConstant.MODIFIED_BY, user.getRealName())
            .set(DbConstant.MODIFIED_ID, user.getUserId());
    }

    private Update getUpdate(T entity) {
        Update update = this.buildFinalAttrByUpdate(entity);

        if (entity.getVersion() != null) {
            update.set(DbConstant.VERSION, entity.getVersion() + 1);
        }

        return update;
    }

    private Update buildFinalAttrByUpdate(T entity) {
        UserBaseVO user = getCurrentUserInfo();

        Map<String, Object> fields = this.getTableField(entity);

        LocalDateTime now = LocalDateTime.now();
        long ts = System.currentTimeMillis();

        fields.put(DbConstant.MODIFIED_TIME, now);
        fields.put(DbConstant.MODIFIED_TIMESTAMP, ts);
        fields.put(DbConstant.MODIFIED_BY, user.getRealName());
        fields.put(DbConstant.MODIFIED_ID, user.getUserId());

        Update update = new Update();
        fields.forEach(update::set);

        return update;
    }

    /**
     * 反射字段提取（只取非空）
     */
    private Map<String, Object> getTableField(T entity) {
        Map<String, Object> resultMap = new HashMap<>(16);

        Field[] fields = entity.getClass().getDeclaredFields();
        if (ArrayUtil.isEmpty(fields)) {
            return resultMap;
        }

        for (Field field : fields) {
            ReflectionUtils.makeAccessible(field);
            String name = field.getName();

            if (DbConstant.ID.equals(name)) {
                continue;
            }

            Object value = ReflectUtil.getFieldValue(entity, field);
            if (value != null) {
                resultMap.put(name, value);
            }
        }

        return resultMap;
    }

    private Criteria getCriteriaDeleted() {
        return Criteria.where(DbConstant.DELETED)
            .is(NumberConstant.NUMBER_ZERO);
    }

    private MongoLambdaQueryWrapper wrapQueryByDelete(MongoLambdaQueryWrapper<T> wrapper) {
        wrapper.eq(DbConstant.DELETED, NumberConstant.NUMBER_ZERO);
        return wrapper;
    }

    private UserBaseVO getCurrentUserInfo() {
        return Optional.ofNullable(UserContextHolder.getContext())
            .orElse(UserContextHolder.getDefaultUser());
    }
}
