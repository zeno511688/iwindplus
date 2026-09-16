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
import com.iwindplus.log.application.query.dto.MailCaptchaLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.MailCaptchaLogSearchDTO;
import com.iwindplus.log.application.query.vo.MailCaptchaLogPageVO;
import com.iwindplus.log.application.query.vo.MailCaptchaLogVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.captcha.MailCaptchaLogDO;
import com.iwindplus.log.infrastructure.persistence.captcha.MailCaptchaLogRepository;
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
 * 邮箱验证码日志查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:53
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_CAPTCHA_LOG})
@RequiredArgsConstructor
public class MailCaptchaLogQueryService {

    private final MailCaptchaLogRepository mailCaptchaLogRepository;
    private final UserClient userClient;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<MailCaptchaLogPageVO>
     */
    public IPage<MailCaptchaLogPageVO> page(MailCaptchaLogSearchDTO entity) {
        final PageDTO<MailCaptchaLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = buildPageWrapper(entity);
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<MailCaptchaLogDO> modelPage = this.mailCaptchaLogRepository.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailCaptchaLogPageVO.class));
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<MailCaptchaLogPageVO>
     */
    public EsPageDTO<MailCaptchaLogPageVO> pageByAfter(MailCaptchaLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<MailCaptchaLogDO> page = EsPageDTO.<MailCaptchaLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<MailCaptchaLogDO> resultPage = this.mailCaptchaLogRepository.pageByAfter(page, wrapper);

        List<MailCaptchaLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = resultPage.getRecords().stream()
                .map(model -> BeanUtil.copyProperties(model, MailCaptchaLogPageVO.class))
                .toList();
        }

        return EsPageDTO.<MailCaptchaLogPageVO>builder()
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
     * @return MailCaptchaLogVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public MailCaptchaLogVO getDetail(String id) {
        MailCaptchaLogDO data = this.mailCaptchaLogRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, MailCaptchaLogVO.class);
    }

    private EsLambdaQueryWrapper<MailCaptchaLogDO> buildPageWrapper(MailCaptchaLogSearchDTO entity) {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(MailCaptchaLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(MailCaptchaLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTplCode())) {
            wrapper.eq(MailCaptchaLogDO::getTplCode, entity.getTplCode());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(MailCaptchaLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank((entity.getMail()))) {
            wrapper.like(MailCaptchaLogDO::getMail, entity.getMail());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(MailCaptchaLogDO::getUserId, entity.getUserId());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<MailCaptchaLogDO> buildPageWrapper(MailCaptchaLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(MailCaptchaLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(MailCaptchaLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTplCode())) {
            wrapper.eq(MailCaptchaLogDO::getTplCode, entity.getTplCode());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(MailCaptchaLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank((entity.getMail()))) {
            wrapper.like(MailCaptchaLogDO::getMail, entity.getMail());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(MailCaptchaLogDO::getUserId, entity.getUserId());
        }
        return wrapper;
    }
}
