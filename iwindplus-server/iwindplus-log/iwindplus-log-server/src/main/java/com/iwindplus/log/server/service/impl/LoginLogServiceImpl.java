/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */


package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.Platform;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.auth.domain.enums.AuthModuleEnum;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.service.address.AddressService;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.domain.dto.LoginLogSearchAfterDTO;
import com.iwindplus.log.domain.dto.LoginLogSearchDTO;
import com.iwindplus.log.domain.vo.LoginLogExtendVO;
import com.iwindplus.log.domain.vo.LoginLogPageVO;
import com.iwindplus.log.domain.vo.LoginLogVO;
import com.iwindplus.log.server.dal.model.LoginLogDO;
import com.iwindplus.log.server.service.LoginLogService;
import com.iwindplus.mgt.client.power.UserClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录日志业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */

@Slf4j
@Service
@CacheConfig(cacheNames = {"loginLog"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class LoginLogServiceImpl extends EsBaseServiceImpl<LoginLogDO>
    implements LoginLogService {

    private final UserClient userClient;
    private final AddressService addressService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(LoginLogDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getIp())) {
            entity.setIp(MDC.get(HeaderConstant.X_REAL_IP));
        }
        if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
            entity.setBizTraceId(MDC.get(HeaderConstant.X_TRACE_ID));
        }
        this.buildUserAgent(entity);
        this.buildDevice(entity);
        this.buildLocation(entity);
        final LoginLogDO model = BeanUtil.copyProperties(entity, LoginLogDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveBatch(List<LoginLogDTO> entities) {
        if (CollUtil.isNotEmpty(entities)) {
            entities.forEach(entity -> {
                if (CharSequenceUtil.isBlank(entity.getIp())) {
                    entity.setIp(MDC.get(HeaderConstant.X_REAL_IP));
                }
                if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
                    entity.setBizTraceId(MDC.get(HeaderConstant.X_TRACE_ID));
                }
                this.buildUserAgent(entity);
                this.buildDevice(entity);
                this.buildLocation(entity);
            });
            super.saveBatch(BeanUtil.copyToList(entities, LoginLogDO.class));
        }
        return false;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<String> ids) {
        List<LoginLogDO> data = super.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @Override
    public IPage<LoginLogPageVO> page(LoginLogSearchDTO entity) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = this.buildPageWrapper(entity);
        final PageDTO<LoginLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<LoginLogDO> modelPage = super.page(page, wrapper);
        final IPage<LoginLogPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, LoginLogPageVO.class));
        List<LoginLogPageVO> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            this.buildUserInfo(result, records);
        }
        return result;
    }

    @Override
    public EsPageDTO<LoginLogPageVO> pageByAfter(LoginLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = this.buildPageWrapper(entity);

        EsPageDTO<LoginLogDO> page = EsPageDTO.<LoginLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<LoginLogDO> resultPage = super.pageByAfter(page, wrapper);

        List<LoginLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = GatewayLogServiceImpl.enrichUserInfo(
                userClient,
                resultPage.getRecords().stream()
                    .map(model -> BeanUtil.copyProperties(model, LoginLogPageVO.class))
                    .toList(),
                LoginLogPageVO::getUserId,
                v -> v::setJobNumber,
                v -> v::setMobile
            );
        }

        return EsPageDTO.<LoginLogPageVO>builder()
            .size(resultPage.getSize())
            .total(resultPage.getTotal())
            .records(voList)
            .searchAfter(resultPage.getSearchAfter())
            .build();
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public LoginLogExtendVO getDetail(String id) {
        LoginLogDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        LoginLogExtendVO result = BeanUtil.copyProperties(data, LoginLogExtendVO.class);
        result.setMobile(GatewayLogServiceImpl.getMobileByUserId(userClient, result.getUserId()));
        this.buildLocation(result);
        return result;
    }

    @Override
    public LoginLogVO getLoginInfo(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = EsWrappers.<LoginLogDO>lambdaQuery()
            .eq(LoginLogDO::getOrgId, orgId)
            .eq(LoginLogDO::getUserId, userId)
            .eq(LoginLogDO::getModuleName, AuthModuleEnum.LOGIN.getValue())
            .orderByDesc(LoginLogDO::getModifiedTimestamp)
            .limit(1);
        LoginLogDO data = super.getOne(wrapper);
        if (Objects.isNull(data)) {
            return new LoginLogVO();
        }

        return BeanUtil.copyProperties(data, LoginLogVO.class);
    }

    private void buildUserAgent(LoginLogDTO entity) {
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

    private void buildDevice(LoginLogDTO entity) {
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

    private void buildLocation(LoginLogDTO entity) {
        if (CharSequenceUtil.isNotBlank(entity.getProvince())
            && CharSequenceUtil.isNotBlank(entity.getCity())) {
            return;
        }
        this.findAddress(entity.getIp()).ifPresent(address -> {
            entity.setProvince(address.getProvince());
            entity.setCity(address.getCity());
        });
    }

    private void buildLocation(LoginLogExtendVO entity) {
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
            final EsLambdaQueryWrapper<LoginLogDO> wrapper = EsWrappers.<LoginLogDO>lambdaQuery()
                .eq(LoginLogDO::getIp, ip)
                .exists(LoginLogDO::getProvince)
                .exists(LoginLogDO::getCity)
                .limit(1);
            final LoginLogDO cached = super.getOne(wrapper);
            if (Objects.nonNull(cached)
                && CharSequenceUtil.isNotBlank(cached.getProvince())
                && CharSequenceUtil.isNotBlank(cached.getCity())) {
                return Optional.of(AddressVO.builder()
                    .ip(ip)
                    .province(cached.getProvince())
                    .city(cached.getCity())
                    .build());
            }
            return this.addressService.getAddress(ip);
        } catch (Exception e) {
            log.error("获取地址信息异常，ip={}", ip, e);
            return Optional.empty();
        }
    }

    private void buildUserInfo(IPage<LoginLogPageVO> result, List<LoginLogPageVO> records) {
        List<LoginLogPageVO> enriched = GatewayLogServiceImpl.enrichUserInfo(userClient, records,
            LoginLogPageVO::getUserId, v -> v::setJobNumber, v -> v::setMobile);
        result.setRecords(enriched);
    }

    private EsLambdaQueryWrapper<LoginLogDO> buildPageWrapper(LoginLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = new EsLambdaQueryWrapper<>();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(LoginLogDO::getRequestId, entity.getRequestId());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(LoginLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getModuleName())) {
            wrapper.eq(LoginLogDO::getModuleName, entity.getModuleName());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(LoginLogDO::getUserId, entity.getUserId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizTraceId())) {
            wrapper.eq(LoginLogDO::getBizTraceId, entity.getBizTraceId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            wrapper.eq(LoginLogDO::getIp, entity.getIp());
        }
        return wrapper;
    }

    private EsLambdaQueryWrapper<LoginLogDO> buildPageWrapper(LoginLogSearchDTO entity) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = EsWrappers.lambdaQuery();
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(LoginLogDO::getRequestId, entity.getRequestId());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(LoginLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getModuleName())) {
            wrapper.eq(LoginLogDO::getModuleName, entity.getModuleName());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(LoginLogDO::getUserId, entity.getUserId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizTraceId())) {
            wrapper.eq(LoginLogDO::getBizTraceId, entity.getBizTraceId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            wrapper.eq(LoginLogDO::getIp, entity.getIp());
        }
        return wrapper;
    }

}
