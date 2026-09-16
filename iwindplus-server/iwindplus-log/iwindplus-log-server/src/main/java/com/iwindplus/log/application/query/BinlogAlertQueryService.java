/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.application.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.log.application.query.dto.BinlogAlertSearchAfterDTO;
import com.iwindplus.log.application.query.dto.BinlogAlertSearchDTO;
import com.iwindplus.log.application.query.vo.BinlogAlertPageVO;
import com.iwindplus.log.application.query.vo.BinlogAlertVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.BinlogAlertDO;
import com.iwindplus.log.infrastructure.persistence.log.BinlogAlertRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * binlog告警查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:50
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_BINLOG_ALERT})
@RequiredArgsConstructor
public class BinlogAlertQueryService {

    private final BinlogAlertRepository binlogAlertRepository;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<BinlogAlertPageVO>
     */
    public IPage<BinlogAlertPageVO> page(BinlogAlertSearchDTO entity) {
        final PageDTO<BinlogAlertDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        final EsLambdaQueryWrapper<BinlogAlertDO> wrapper = buildPageWrapper(entity);
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<BinlogAlertDO> modelPage = this.binlogAlertRepository.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, BinlogAlertPageVO.class));
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<BinlogAlertPageVO>
     */
    public EsPageDTO<BinlogAlertPageVO> pageByAfter(BinlogAlertSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<BinlogAlertDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<BinlogAlertDO> page = EsPageDTO.<BinlogAlertDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<BinlogAlertDO> resultPage = this.binlogAlertRepository.pageByAfter(page, wrapper);

        List<BinlogAlertPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = resultPage.getRecords().stream()
                .map(model -> BeanUtil.copyProperties(model, BinlogAlertPageVO.class))
                .toList();
        }

        return EsPageDTO.<BinlogAlertPageVO>builder()
            .size(resultPage.getSize())
            .total(resultPage.getTotal())
            .pages(resultPage.getPages())
            .records(voList)
            .searchAfter(resultPage.getSearchAfter())
            .build();
    }

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return BinlogAlertVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public BinlogAlertVO getDetail(String id) {
        BinlogAlertDO data = this.binlogAlertRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, BinlogAlertVO.class);
    }

    private EsLambdaQueryWrapper<BinlogAlertDO> buildPageWrapper(BinlogAlertSearchDTO entity) {
        final EsLambdaQueryWrapper<BinlogAlertDO> wrapper = EsWrappers.lambdaQuery();
        if (Objects.nonNull(entity.getDataId())) {
            wrapper.eq(BinlogAlertDO::getDataId, entity.getDataId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getDb())) {
            wrapper.eq(BinlogAlertDO::getDb, entity.getDb());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTable())) {
            wrapper.eq(BinlogAlertDO::getTable, entity.getTable());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<BinlogAlertDO> buildPageWrapper(BinlogAlertSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<BinlogAlertDO> wrapper = EsWrappers.lambdaQuery();
        if (Objects.nonNull(entity.getDataId())) {
            wrapper.eq(BinlogAlertDO::getDataId, entity.getDataId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getDb())) {
            wrapper.eq(BinlogAlertDO::getDb, entity.getDb());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTable())) {
            wrapper.eq(BinlogAlertDO::getTable, entity.getTable());
        }
        return wrapper;
    }
}
