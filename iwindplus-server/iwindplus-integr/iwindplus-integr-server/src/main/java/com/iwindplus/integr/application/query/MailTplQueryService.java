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
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.integr.common.constant.IntegrConstant.RedisCacheConstant;
import com.iwindplus.integr.application.query.dto.MailTplSearchDTO;
import com.iwindplus.integr.application.query.vo.MailTplBaseVO;
import com.iwindplus.integr.application.query.vo.MailTplPageVO;
import com.iwindplus.integr.application.query.vo.MailTplVO;
import com.iwindplus.integr.infrastructure.persistence.MailTplDO;
import com.iwindplus.integr.infrastructure.persistence.MailTplRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 邮件模版查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 12:24
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SMS_TPL})
@RequiredArgsConstructor
public class MailTplQueryService {

    private final MailTplRepository mailTplRepository;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<MailTplPageVO>
     */
    public IPage<MailTplPageVO> page(PageDTO<MailTplDO> page, MailTplSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<MailTplDO> queryWrapper = Wrappers.lambdaQuery(MailTplDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(MailTplDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(MailTplDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(MailTplDO::getName, entity.getName().trim());
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
        queryWrapper.select(MailTplDO::getId, MailTplDO::getCreatedTimestamp, MailTplDO::getCreatedBy,
            MailTplDO::getModifiedTimestamp, MailTplDO::getModifiedBy, MailTplDO::getVersion, MailTplDO::getRemark,
            MailTplDO::getBuildInFlag, MailTplDO::getStatus, MailTplDO::getCode, MailTplDO::getName
        );
        final PageDTO<MailTplDO> modelPage = this.mailTplRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailTplPageVO.class));
    }

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return MailTplVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public MailTplVO getByCode(String code) {
        return this.mailTplRepository.getByCode(code);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return MailTplVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public MailTplVO getDetail(Long id) {
        MailTplDO data = this.mailTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final MailTplVO result = BeanUtil.copyProperties(data, MailTplVO.class);
        return result;
    }

    /**
     * 启用的列表.
     *
     * @return List<MailTplBaseVO>
     */
    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<MailTplBaseVO> listEnabled() {
        final List<MailTplDO> list = this.mailTplRepository.list(Wrappers.lambdaQuery(MailTplDO.class)
            .eq(MailTplDO::getStatus, EnableStatusEnum.ENABLE)
            .orderByDesc(MailTplDO::getModifiedTimestamp));
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, MailTplBaseVO.class);
    }

}
