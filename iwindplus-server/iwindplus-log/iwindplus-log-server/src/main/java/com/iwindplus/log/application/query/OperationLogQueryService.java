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
import com.iwindplus.base.address.domain.vo.AddressVO;
import com.iwindplus.base.address.factory.AddressExecuteHandlerFactory;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.log.application.query.dto.OperationLogNewestDTO;
import com.iwindplus.log.application.query.dto.OperationLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.OperationLogSearchDTO;
import com.iwindplus.log.application.query.vo.OperationLogExtendVO;
import com.iwindplus.log.application.query.vo.OperationLogPageVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.OperationLogDO;
import com.iwindplus.log.infrastructure.persistence.log.OperationLogRepository;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 操作日志查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:53
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_OPERATION_LOG})
@RequiredArgsConstructor
public class OperationLogQueryService {

    private final OperationLogRepository operationLogRepository;
    private final UserClient userClient;
    private final AddressExecuteHandlerFactory addressExecuteHandlerFactory;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<OperationLogPageVO>
     */
    public IPage<OperationLogPageVO> page(OperationLogSearchDTO entity) {
        final PageDTO<OperationLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = buildPageWrapper(entity);
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<OperationLogDO> modelPage = this.operationLogRepository.page(page, wrapper);
        final IPage<OperationLogPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, OperationLogPageVO.class));
        List<OperationLogPageVO> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            this.buildUserInfo(result, records);
        }
        return result;
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<OperationLogPageVO>
     */
    public EsPageDTO<OperationLogPageVO> pageByAfter(OperationLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<OperationLogDO> page = EsPageDTO.<OperationLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<OperationLogDO> resultPage = this.operationLogRepository.pageByAfter(page, wrapper);

        List<OperationLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = GatewayLogQueryService.enrichUserInfo(
                userClient,
                resultPage.getRecords().stream()
                    .map(model -> BeanUtil.copyProperties(model, OperationLogPageVO.class))
                    .toList(),
                OperationLogPageVO::getUserId,
                v -> v::setJobNumber,
                v -> v::setMobile
            );
        }

        return EsPageDTO.<OperationLogPageVO>builder()
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
     * @return OperationLogExtendVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public OperationLogExtendVO getDetail(String id) {
        OperationLogDO data = this.operationLogRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        OperationLogExtendVO result = BeanUtil.copyProperties(data, OperationLogExtendVO.class);
        result.setMobile(GatewayLogQueryService.getMobileByUserId(userClient, result.getUserId()));
        this.buildLocation(result);
        return result;
    }

    /**
     * 根据条件获取最新数据.
     *
     * @param entity 对象
     * @return OperationLogExtendVO
     */
    public OperationLogExtendVO getNewestByCondition(OperationLogNewestDTO entity) {
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = EsWrappers.<OperationLogDO>lambdaQuery()
            .eq(OperationLogDO::getOrgId, entity.getOrgId())
            .eq(OperationLogDO::getUserId, entity.getCurrentUserId())
            .eq(OperationLogDO::getBizNumber, entity.getBizNumber())
            .eq(OperationLogDO::getBizType, entity.getBizType())
            .eq(OperationLogDO::getOperateType, entity.getOperateType())
            .eq(OperationLogDO::getOperateName, entity.getOperateName())
            .orderByDesc(OperationLogDO::getModifiedTimestamp)
            .limit(1);
        OperationLogDO data = this.operationLogRepository.getOne(wrapper);
        if (Objects.isNull(data)) {
            return new OperationLogExtendVO();
        }
        OperationLogExtendVO result = BeanUtil.copyProperties(data, OperationLogExtendVO.class);
        this.buildLocation(result);
        return result;
    }

    private void buildLocation(OperationLogExtendVO entity) {
        if (CharSequenceUtil.isNotBlank(entity.getProvince())
            && CharSequenceUtil.isNotBlank(entity.getCity())) {
            return;
        }
        this.findAddress(entity.getIp()).ifPresent(address -> {
            entity.setProvince(address.getProvince());
            entity.setCity(address.getCity());
        });
    }

    private Optional<AddressVO> findAddress(String ip) {
        if (CharSequenceUtil.isBlank(ip)) {
            return Optional.empty();
        }
        try {
            final EsLambdaQueryWrapper<OperationLogDO> wrapper = EsWrappers.<OperationLogDO>lambdaQuery()
                .eq(OperationLogDO::getIp, ip)
                .exists(OperationLogDO::getProvince)
                .exists(OperationLogDO::getCity)
                .limit(1);
            final OperationLogDO cached = this.operationLogRepository.getOne(wrapper);
            if (Objects.nonNull(cached)
                && CharSequenceUtil.isNotBlank(cached.getProvince())
                && CharSequenceUtil.isNotBlank(cached.getCity())) {
                return Optional.of(AddressVO.builder()
                    .ip(ip)
                    .province(cached.getProvince())
                    .city(cached.getCity())
                    .build());
            }

            return this.addressExecuteHandlerFactory.getAddress(ip);
        } catch (Exception e) {
            log.error("获取地址信息异常，ip={}", ip, e);
            return Optional.empty();
        }
    }

    private void buildUserInfo(IPage<OperationLogPageVO> result, List<OperationLogPageVO> records) {
        List<OperationLogPageVO> enriched = GatewayLogQueryService.enrichUserInfo(userClient, records,
            OperationLogPageVO::getUserId, v -> v::setJobNumber, v -> v::setMobile);
        result.setRecords(enriched);
    }

    private EsLambdaQueryWrapper<OperationLogDO> buildPageWrapper(OperationLogSearchDTO entity) {
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(OperationLogDO::getRequestId, entity.getRequestId());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(OperationLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(OperationLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getOperateName())) {
            wrapper.eq(OperationLogDO::getOperateName, entity.getOperateName());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(OperationLogDO::getUserId, entity.getUserId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizTraceId())) {
            wrapper.eq(OperationLogDO::getBizTraceId, entity.getBizTraceId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            wrapper.eq(OperationLogDO::getIp, entity.getIp());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<OperationLogDO> buildPageWrapper(OperationLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(OperationLogDO::getRequestId, entity.getRequestId());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(OperationLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(OperationLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getOperateName())) {
            wrapper.eq(OperationLogDO::getOperateName, entity.getOperateName());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(OperationLogDO::getUserId, entity.getUserId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizTraceId())) {
            wrapper.eq(OperationLogDO::getBizTraceId, entity.getBizTraceId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            wrapper.eq(OperationLogDO::getIp, entity.getIp());
        }
        return wrapper;
    }
}
