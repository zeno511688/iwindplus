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
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.log.application.query.dto.GatewayLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.GatewayLogSearchDTO;
import com.iwindplus.log.application.query.vo.GatewayLogExtendVO;
import com.iwindplus.log.application.query.vo.GatewayLogPageVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.GatewayLogDO;
import com.iwindplus.log.infrastructure.persistence.log.GatewayLogRepository;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 网关日志查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:51
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_GATEWAY_LOG})
@RequiredArgsConstructor
public class GatewayLogQueryService {

    private final GatewayLogRepository gatewayLogRepository;
    private final UserClient userClient;
    private final AddressExecuteHandlerFactory addressExecuteHandlerFactory;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<GatewayLogPageVO>
     */
    public IPage<GatewayLogPageVO> page(GatewayLogSearchDTO entity) {
        final PageDTO<GatewayLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        final EsLambdaQueryWrapper<GatewayLogDO> wrapper = buildPageWrapper(entity);
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<GatewayLogDO> modelPage = this.gatewayLogRepository.page(page, wrapper);
        final IPage<GatewayLogPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, GatewayLogPageVO.class));
        List<GatewayLogPageVO> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            this.buildUserInfo(result, records);
        }
        return result;
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<GatewayLogPageVO>
     */
    public EsPageDTO<GatewayLogPageVO> pageByAfter(GatewayLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<GatewayLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<GatewayLogDO> page = EsPageDTO.<GatewayLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<GatewayLogDO> resultPage = this.gatewayLogRepository.pageByAfter(page, wrapper);

        List<GatewayLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = GatewayLogQueryService.enrichUserInfo(
                userClient,
                resultPage.getRecords().stream()
                    .map(model -> BeanUtil.copyProperties(model, GatewayLogPageVO.class))
                    .toList(),
                GatewayLogPageVO::getUserId,
                v -> v::setJobNumber,
                v -> v::setMobile
            );
        }

        return EsPageDTO.<GatewayLogPageVO>builder()
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
     * @return GatewayLogExtendVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public GatewayLogExtendVO getDetail(String id) {
        GatewayLogDO data = this.gatewayLogRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        GatewayLogExtendVO result = BeanUtil.copyProperties(data, GatewayLogExtendVO.class);
        result.setMobile(GatewayLogQueryService.getMobileByUserId(userClient, result.getUserId()));
        this.buildLocation(result);
        return result;
    }

    private void buildLocation(GatewayLogExtendVO entity) {
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
            final EsLambdaQueryWrapper<GatewayLogDO> wrapper = EsWrappers.<GatewayLogDO>lambdaQuery()
                .eq(GatewayLogDO::getIp, ip)
                .exists(GatewayLogDO::getProvince)
                .exists(GatewayLogDO::getCity)
                .limit(1);
            final GatewayLogDO cached = this.gatewayLogRepository.getOne(wrapper);
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

    private void buildUserInfo(IPage<GatewayLogPageVO> result, List<GatewayLogPageVO> records) {
        List<GatewayLogPageVO> enriched = GatewayLogQueryService.enrichUserInfo(userClient, records,
            GatewayLogPageVO::getUserId, v -> v::setJobNumber, v -> v::setMobile);
        result.setRecords(enriched);
    }

    static <T> List<T> enrichUserInfo(
        UserClient userClient,
        List<T> records,
        Function<T, Long> userIdGetter,
        Function<T, Consumer<String>> jobNumberSetter,
        Function<T, Consumer<String>> mobileSetter) {
        final List<Long> ids = records.stream().filter(Objects::nonNull).map(userIdGetter).filter(Objects::nonNull).toList();
        if (CollUtil.isEmpty(ids)) {
            return records;
        }
        List<UserVO> userList = GatewayLogQueryService.getUserList(userClient, ids);
        if (CollUtil.isEmpty(userList)) {
            return records;
        }
        Map<Long, UserVO> userMap = userList.stream()
            .filter(Objects::nonNull).collect(Collectors.toMap(UserVO::getId, Function.identity()));
        return records.stream()
            .filter(Objects::nonNull)
            .peek(m -> {
                UserVO u = userMap.get(userIdGetter.apply(m));
                if (u != null) {
                    Consumer<String> jobSetter = jobNumberSetter.apply(m);
                    if (jobSetter != null) {
                        jobSetter.accept(u.getJobNumber());
                    }
                    Consumer<String> mobSetter = mobileSetter.apply(m);
                    if (mobSetter != null) {
                        mobSetter.accept(u.getMobile());
                    }
                }
            }).toList();
    }

    static List<UserVO> getUserList(UserClient userClient, List<Long> ids) {
        try {
            return Optional.ofNullable(userClient.listInfoByIds(ids)).map(ResultVO::getBizData).orElse(null);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    static String getMobileByUserId(UserClient userClient, Long userId) {
        final List<UserVO> userList = GatewayLogQueryService.getUserList(userClient, List.of(userId));
        return CollUtil.isEmpty(userList) ? null : Optional.ofNullable(userList.get(0)).map(UserVO::getMobile).orElse(null);
    }

    static Long getUserIdByJobNumber(UserClient userClient, String jobNumber) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().jobNumber(jobNumber).build();
        try {
            return Optional.ofNullable(userClient.getByCondition(entity)).map(ResultVO::getBizData).map(UserVO::getId).orElse(null);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    static Long getUserIdByMobile(UserClient userClient, String mobile) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mobile(mobile).build();
        try {
            return Optional.ofNullable(userClient.getByCondition(entity)).map(ResultVO::getBizData).map(UserVO::getId).orElse(null);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    private EsLambdaQueryWrapper<GatewayLogDO> buildPageWrapper(GatewayLogSearchDTO entity) {
        final EsLambdaQueryWrapper<GatewayLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(GatewayLogDO::getRequestId, entity.getRequestId());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(GatewayLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTargetServer())) {
            wrapper.eq(GatewayLogDO::getTargetServer, entity.getTargetServer());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRequestPath())) {
            wrapper.like(GatewayLogDO::getRequestPath, entity.getRequestPath());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(GatewayLogDO::getUserId, entity.getUserId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizTraceId())) {
            wrapper.eq(GatewayLogDO::getBizTraceId, entity.getBizTraceId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            wrapper.like(GatewayLogDO::getIp, entity.getIp());
        }
        if (Objects.nonNull(entity.getResponseStatus())) {
            wrapper.eq(GatewayLogDO::getResponseStatus, entity.getResponseStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getResponseErrorCode())) {
            wrapper.eq(GatewayLogDO::getResponseErrorCode, entity.getResponseErrorCode());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<GatewayLogDO> buildPageWrapper(GatewayLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<GatewayLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(GatewayLogDO::getRequestId, entity.getRequestId());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(GatewayLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTargetServer())) {
            wrapper.eq(GatewayLogDO::getTargetServer, entity.getTargetServer());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRequestPath())) {
            wrapper.like(GatewayLogDO::getRequestPath, entity.getRequestPath());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(GatewayLogDO::getUserId, entity.getUserId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizTraceId())) {
            wrapper.eq(GatewayLogDO::getBizTraceId, entity.getBizTraceId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            wrapper.like(GatewayLogDO::getIp, entity.getIp());
        }
        if (Objects.nonNull(entity.getResponseStatus())) {
            wrapper.eq(GatewayLogDO::getResponseStatus, entity.getResponseStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getResponseErrorCode())) {
            wrapper.eq(GatewayLogDO::getResponseErrorCode, entity.getResponseErrorCode());
        }
        return wrapper;
    }
}
