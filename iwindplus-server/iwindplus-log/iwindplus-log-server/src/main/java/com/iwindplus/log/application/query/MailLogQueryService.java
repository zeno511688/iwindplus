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
import com.iwindplus.log.application.query.dto.MailLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.MailLogSearchDTO;
import com.iwindplus.log.application.query.vo.MailLogPageVO;
import com.iwindplus.log.application.query.vo.MailLogVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.MailLogDO;
import com.iwindplus.log.infrastructure.persistence.log.MailLogRepository;
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
 * 邮箱日志查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:53
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_LOG})
@RequiredArgsConstructor
public class MailLogQueryService {

    private final MailLogRepository mailLogRepository;
    private final UserClient userClient;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<MailLogPageVO>
     */
    public IPage<MailLogPageVO> page(MailLogSearchDTO entity) {
        final PageDTO<MailLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        final EsLambdaQueryWrapper<MailLogDO> wrapper = buildPageWrapper(entity);
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<MailLogDO> modelPage = this.mailLogRepository.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailLogPageVO.class));
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<MailLogPageVO>
     */
    public EsPageDTO<MailLogPageVO> pageByAfter(MailLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<MailLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<MailLogDO> page = EsPageDTO.<MailLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<MailLogDO> resultPage = this.mailLogRepository.pageByAfter(page, wrapper);

        List<MailLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = resultPage.getRecords().stream()
                .map(model -> BeanUtil.copyProperties(model, MailLogPageVO.class))
                .toList();
        }

        return EsPageDTO.<MailLogPageVO>builder()
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
     * @return MailLogVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public MailLogVO getDetail(String id) {
        MailLogDO data = this.mailLogRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, MailLogVO.class);
    }

    private EsLambdaQueryWrapper<MailLogDO> buildPageWrapper(MailLogSearchDTO entity) {
        final EsLambdaQueryWrapper<MailLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(MailLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(MailLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank((entity.getSubject()))) {
            wrapper.eq(MailLogDO::getSubject, entity.getSubject());
        }
        if (CharSequenceUtil.isNotBlank((entity.getContent()))) {
            wrapper.like(MailLogDO::getContent, entity.getContent());
        }
        if (CharSequenceUtil.isNotBlank((entity.getNickName()))) {
            wrapper.eq(MailLogDO::getNickName, entity.getNickName());
        }
        if (CharSequenceUtil.isNotBlank((entity.getUsername()))) {
            wrapper.eq(MailLogDO::getUsername, entity.getUsername());
        }
        if (CharSequenceUtil.isNotBlank((entity.getTos()))) {
            wrapper.like(MailLogDO::getTos, entity.getTos());
        }
        if (Objects.nonNull(entity.getResult())) {
            wrapper.eq(MailLogDO::getResult, entity.getResult());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(MailLogDO::getUserId, entity.getUserId());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<MailLogDO> buildPageWrapper(MailLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<MailLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(MailLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(MailLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank((entity.getSubject()))) {
            wrapper.eq(MailLogDO::getSubject, entity.getSubject());
        }
        if (CharSequenceUtil.isNotBlank((entity.getContent()))) {
            wrapper.like(MailLogDO::getContent, entity.getContent());
        }
        if (CharSequenceUtil.isNotBlank((entity.getNickName()))) {
            wrapper.eq(MailLogDO::getNickName, entity.getNickName());
        }
        if (CharSequenceUtil.isNotBlank((entity.getUsername()))) {
            wrapper.eq(MailLogDO::getUsername, entity.getUsername());
        }
        if (CharSequenceUtil.isNotBlank((entity.getTos()))) {
            wrapper.like(MailLogDO::getTos, entity.getTos());
        }
        if (Objects.nonNull(entity.getResult())) {
            wrapper.eq(MailLogDO::getResult, entity.getResult());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(MailLogDO::getUserId, entity.getUserId());
        }
        return wrapper;
    }
}
