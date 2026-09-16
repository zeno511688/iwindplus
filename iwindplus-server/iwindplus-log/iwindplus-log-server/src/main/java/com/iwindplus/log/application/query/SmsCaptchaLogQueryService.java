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
import com.iwindplus.log.application.query.dto.SmsCaptchaLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.SmsCaptchaLogSearchDTO;
import com.iwindplus.log.application.query.vo.SmsCaptchaLogPageVO;
import com.iwindplus.log.application.query.vo.SmsCaptchaLogVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.captcha.SmsCaptchaLogDO;
import com.iwindplus.log.infrastructure.persistence.captcha.SmsCaptchaLogRepository;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 短信验证码日志查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:53
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SMS_CAPTCHA_LOG})
@RequiredArgsConstructor
public class SmsCaptchaLogQueryService {

    private final SmsCaptchaLogRepository smsCaptchaLogRepository;
    private final UserClient userClient;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<SmsCaptchaLogPageVO>
     */
    public IPage<SmsCaptchaLogPageVO> page(SmsCaptchaLogSearchDTO entity) {
        final PageDTO<SmsCaptchaLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper = buildPageWrapper(entity);
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<SmsCaptchaLogDO> modelPage = this.smsCaptchaLogRepository.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, SmsCaptchaLogPageVO.class));
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<SmsCaptchaLogPageVO>
     */
    public EsPageDTO<SmsCaptchaLogPageVO> pageByAfter(SmsCaptchaLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<SmsCaptchaLogDO> page = EsPageDTO.<SmsCaptchaLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<SmsCaptchaLogDO> resultPage = this.smsCaptchaLogRepository.pageByAfter(page, wrapper);

        List<SmsCaptchaLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = resultPage.getRecords().stream()
                .map(model -> BeanUtil.copyProperties(model, SmsCaptchaLogPageVO.class))
                .toList();
        }

        return EsPageDTO.<SmsCaptchaLogPageVO>builder()
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
     * @return SmsCaptchaLogVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public SmsCaptchaLogVO getDetail(String id) {
        SmsCaptchaLogDO data = this.smsCaptchaLogRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, SmsCaptchaLogVO.class);
    }

    private EsLambdaQueryWrapper<SmsCaptchaLogDO> buildPageWrapper(SmsCaptchaLogSearchDTO entity) {
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(SmsCaptchaLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(SmsCaptchaLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTplCode())) {
            wrapper.eq(SmsCaptchaLogDO::getTplCode, entity.getTplCode());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(SmsCaptchaLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank((entity.getMobile()))) {
            wrapper.like(SmsCaptchaLogDO::getMobile, entity.getMobile());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(SmsCaptchaLogDO::getUserId, entity.getUserId());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<SmsCaptchaLogDO> buildPageWrapper(SmsCaptchaLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(SmsCaptchaLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(SmsCaptchaLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTplCode())) {
            wrapper.eq(SmsCaptchaLogDO::getTplCode, entity.getTplCode());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(SmsCaptchaLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank((entity.getMobile()))) {
            wrapper.like(SmsCaptchaLogDO::getMobile, entity.getMobile());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(SmsCaptchaLogDO::getUserId, entity.getUserId());
        }
        return wrapper;
    }
}
