/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.Platform;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.iwindplus.base.address.domain.vo.AddressVO;
import com.iwindplus.base.address.factory.AddressExecuteHandlerFactory;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.log.api.dto.OperationLogDTO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.OperationLogDO;
import com.iwindplus.log.infrastructure.persistence.log.OperationLogRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志业务层.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_OPERATION_LOG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OperationLogApplicationService {

    private final OperationLogRepository operationLogRepository;
    private final AddressExecuteHandlerFactory addressExecuteHandlerFactory;

    /**
     * 保存
     *
     * @param entity    对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean save(OperationLogDTO entity) {
        final Map<String, String> headerMap = HeaderContextHolder.getContext();
        if (CharSequenceUtil.isBlank(entity.getRequestId())) {
            String requestId = Optional.ofNullable(headerMap)
                .map(map -> map.get(HeaderConstant.X_REQUESTED_ID))
                .filter(CharSequenceUtil::isNotBlank)
                .orElseGet(() -> MDC.get(HeaderConstant.X_REQUESTED_ID));
            entity.setRequestId(requestId);
        }
        if (CharSequenceUtil.isBlank(entity.getIp())) {
            String ip = Optional.ofNullable(headerMap)
                .map(map -> map.get(HeaderConstant.X_REAL_IP))
                .filter(CharSequenceUtil::isNotBlank)
                .orElseGet(() -> MDC.get(HeaderConstant.X_REAL_IP));
            entity.setIp(ip);
        }
        if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
            entity.setBizTraceId(MDC.get(HeaderConstant.X_TRACE_ID));
        }
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(IdUtil.simpleUUID());
        }
        this.buildUserAgent(entity);
        this.buildDevice(entity);
        this.buildLocation(entity);
        if (CharSequenceUtil.isNotBlank(entity.getRequestParam())) {
            String str = HtmlUtil.unescape(entity.getRequestParam());
            entity.setRequestParam(str);
        }
        if (CharSequenceUtil.isNotBlank(entity.getRequestBody())) {
            String str = HtmlUtil.unescape(entity.getRequestBody());
            entity.setRequestBody(str);
        }
        if (CharSequenceUtil.isNotBlank(entity.getResponseBody())) {
            String str = HtmlUtil.unescape(entity.getResponseBody());
            entity.setResponseBody(str);
        }
        final OperationLogDO model = BeanUtil.copyProperties(entity, OperationLogDO.class);
        this.operationLogRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<String> ids) {
        List<OperationLogDO> data = this.operationLogRepository.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.operationLogRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    private void buildUserAgent(OperationLogDTO entity) {
        if (CharSequenceUtil.isNotBlank(entity.getPlatformName())
            && CharSequenceUtil.isNotBlank(entity.getOsName())
            && CharSequenceUtil.isNotBlank(entity.getOsVersion())
            && CharSequenceUtil.isNotBlank(entity.getBrowserName())
            && CharSequenceUtil.isNotBlank(entity.getBrowserVersion())) {
            return;
        }

        final String userAgentStr = Optional.ofNullable(HeaderContextHolder.getContext())
            .map(context -> context.get(HttpHeaders.USER_AGENT))
            .orElse(null);
        if (CharSequenceUtil.isBlank(userAgentStr)) {
            return;
        }

        final UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        if (Objects.isNull(userAgent)) {
            return;
        }

        final Platform platform = userAgent.getPlatform();
        if (CharSequenceUtil.isBlank(entity.getPlatformName()) && Objects.nonNull(platform)) {
            entity.setPlatformName(platform.getName());
        }

        final OS os = userAgent.getOs();
        if (Objects.nonNull(os)) {
            if (CharSequenceUtil.isBlank(entity.getOsName())) {
                entity.setOsName(os.getName());
            }
            if (CharSequenceUtil.isBlank(entity.getOsVersion())) {
                entity.setOsVersion(os.getVersion(userAgentStr));
            }
        }

        final Browser browser = userAgent.getBrowser();
        if (Objects.nonNull(browser)) {
            if (CharSequenceUtil.isBlank(entity.getBrowserName())) {
                entity.setBrowserName(browser.getName());
            }
            if (CharSequenceUtil.isBlank(entity.getBrowserVersion())) {
                entity.setBrowserVersion(browser.getVersion(userAgentStr));
            }
        }
    }

    private void buildDevice(OperationLogDTO entity) {
        if (CharSequenceUtil.isNotBlank(entity.getDeviceNumber())
            && CharSequenceUtil.isNotBlank(entity.getDeviceVersion())
            && CharSequenceUtil.isNotBlank(entity.getDeviceFingerprint())) {
            return;
        }

        final Map<String, String> headerMap = HeaderContextHolder.getContext();
        if (MapUtil.isEmpty(headerMap)) {
            return;
        }

        entity.setDeviceNumber(headerMap.get(HeaderConstant.X_DEVICE_NUMBER));
        entity.setDeviceVersion(headerMap.get(HeaderConstant.X_DEVICE_VERSION));
        entity.setDeviceFingerprint(headerMap.get(HeaderConstant.X_DEVICE_FINGERPRINT));
    }

    private void buildLocation(OperationLogDTO entity) {
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
}
