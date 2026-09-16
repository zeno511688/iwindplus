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
import com.iwindplus.integr.application.query.dto.SmsTplSearchDTO;
import com.iwindplus.integr.application.query.vo.SmsTplPageVO;
import com.iwindplus.integr.application.query.vo.SmsTplVO;
import com.iwindplus.integr.infrastructure.persistence.SmsTplDO;
import com.iwindplus.integr.infrastructure.persistence.SmsTplRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 短信模版查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 12:25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SMS_TPL})
@RequiredArgsConstructor
public class SmsTplQueryService {

    private final SmsTplRepository smsTplRepository;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<SmsTplPageVO>
     */
    public IPage<SmsTplPageVO> page(PageDTO<SmsTplDO> page, SmsTplSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<SmsTplDO> queryWrapper = Wrappers.lambdaQuery(SmsTplDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(SmsTplDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(SmsTplDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(SmsTplDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getSignName())) {
            queryWrapper.eq(SmsTplDO::getSignName, entity.getSignName().trim());
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
        queryWrapper.select(SmsTplDO::getId, SmsTplDO::getCreatedTimestamp, SmsTplDO::getCreatedBy,
            SmsTplDO::getModifiedTimestamp, SmsTplDO::getModifiedBy, SmsTplDO::getBuildInFlag, SmsTplDO::getVersion,
            SmsTplDO::getRemark, SmsTplDO::getStatus, SmsTplDO::getType, SmsTplDO::getCode, SmsTplDO::getName, SmsTplDO::getSignName
        );
        final PageDTO<SmsTplDO> modelPage = this.smsTplRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, SmsTplPageVO.class));
    }

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return SmsTplVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public SmsTplVO getByCode(String code) {
        return this.smsTplRepository.getByCode(code);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return SmsTplVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public SmsTplVO getDetail(Long id) {
        SmsTplDO data = this.smsTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, SmsTplVO.class);
    }
}
