/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service.impl;

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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
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
import com.iwindplus.log.domain.dto.OperationLogDTO;
import com.iwindplus.log.domain.dto.OperationLogNewestDTO;
import com.iwindplus.log.domain.dto.OperationLogSearchAfterDTO;
import com.iwindplus.log.domain.dto.OperationLogSearchDTO;
import com.iwindplus.log.domain.vo.OperationLogExtendVO;
import com.iwindplus.log.domain.vo.OperationLogPageVO;
import com.iwindplus.log.server.dal.model.OperationLogDO;
import com.iwindplus.log.server.service.OperationLogService;
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
 * 操作日志业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/4/10
 */

@Slf4j
@Service
@CacheConfig(cacheNames = {"operationLog"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OperationLogServiceImpl extends EsBaseServiceImpl<OperationLogDO>
    implements OperationLogService {

    private final UserClient userClient;
    private final AddressService addressService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(OperationLogDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getIp())) {
            entity.setIp(MDC.get(HeaderConstant.X_REAL_IP));
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
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<String> ids) {
        List<OperationLogDO> data = super.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @Override
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
        final IPage<OperationLogDO> modelPage = super.page(page, wrapper);
        final IPage<OperationLogPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, OperationLogPageVO.class));
        List<OperationLogPageVO> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            this.buildUserInfo(result, records);
        }
        return result;
    }

    @Override
    public EsPageDTO<OperationLogPageVO> pageByAfter(OperationLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<OperationLogDO> page = EsPageDTO.<OperationLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<OperationLogDO> resultPage = super.pageByAfter(page, wrapper);

        List<OperationLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = GatewayLogServiceImpl.enrichUserInfo(
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
            .records(voList)
            .searchAfter(resultPage.getSearchAfter())
            .build();
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public OperationLogExtendVO getDetail(String id) {
        OperationLogDO data = this.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        OperationLogExtendVO result = BeanUtil.copyProperties(data, OperationLogExtendVO.class);
        result.setMobile(GatewayLogServiceImpl.getMobileByUserId(userClient, result.getUserId()));
        this.buildLocation(result);
        return result;
    }

    @Override
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
        OperationLogDO data = super.getOne(wrapper);
        if (Objects.isNull(data)) {
            return new OperationLogExtendVO();
        }
        OperationLogExtendVO result = BeanUtil.copyProperties(data, OperationLogExtendVO.class);
        this.buildLocation(result);
        return result;
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
            final OperationLogDO cached = super.getOne(wrapper);
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

    private void buildUserInfo(IPage<OperationLogPageVO> result, List<OperationLogPageVO> records) {
        List<OperationLogPageVO> enriched = GatewayLogServiceImpl.enrichUserInfo(userClient, records,
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
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
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
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
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
