/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.integr.common.constant.IntegrConstant.RedisCacheConstant;
import com.iwindplus.integr.application.query.dto.OssTplSearchDTO;
import com.iwindplus.integr.application.query.vo.OssTplPageVO;
import com.iwindplus.integr.application.query.vo.OssTplVO;
import com.iwindplus.integr.infrastructure.persistence.OssTplDO;
import com.iwindplus.integr.infrastructure.persistence.OssTplRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 对象存储模版查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 12:25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_OSS_TPL})
@RequiredArgsConstructor
public class OssTplQueryService {

    private final OssTplRepository ossTplRepository;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<OssTplPageVO>
     */
    public IPage<OssTplPageVO> page(PageDTO<OssTplDO> page, OssTplSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<OssTplDO> queryWrapper = Wrappers.lambdaQuery(OssTplDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(OssTplDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(OssTplDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(OssTplDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBucketName())) {
            queryWrapper.eq(OssTplDO::getBucketName, entity.getBucketName().trim());
        }
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(DbConstant.MODIFIED_TIMESTAMP);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(OssTplDO::getId, OssTplDO::getCreatedTimestamp, OssTplDO::getCreatedBy,
            OssTplDO::getModifiedTimestamp, OssTplDO::getModifiedBy, OssTplDO::getBuildInFlag, OssTplDO::getVersion,
            OssTplDO::getRemark, OssTplDO::getStatus, OssTplDO::getType, OssTplDO::getCode, OssTplDO::getName, OssTplDO::getBucketName
        );
        final PageDTO<OssTplDO> modelPage = this.ossTplRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, OssTplPageVO.class));
    }

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return OssTplVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public OssTplVO getByCode(String code) {
        return this.ossTplRepository.getByCode(code);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return OssTplVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public OssTplVO getDetail(Long id) {
        OssTplDO data = this.ossTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, OssTplVO.class);
    }
}
