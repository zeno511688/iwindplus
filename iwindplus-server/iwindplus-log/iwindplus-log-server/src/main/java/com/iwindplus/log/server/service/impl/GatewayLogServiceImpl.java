/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.Platform;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.service.address.AddressService;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.domain.dto.GatewayLogSearchAfterDTO;
import com.iwindplus.log.domain.dto.GatewayLogSearchDTO;
import com.iwindplus.log.domain.vo.GatewayLogExtendVO;
import com.iwindplus.log.domain.vo.GatewayLogPageVO;
import com.iwindplus.log.server.dal.model.GatewayLogDO;
import com.iwindplus.log.server.dal.model.LoginLogDO;
import com.iwindplus.log.server.service.GatewayLogService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
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
 * 网关日志业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */

@Slf4j
@Service
@CacheConfig(cacheNames = {"gatewayLog"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class GatewayLogServiceImpl extends EsBaseServiceImpl<GatewayLogDO> implements GatewayLogService {

    private final UserClient userClient;
    private final AddressService addressService;

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveBatch(List<GatewayLogDTO> entities) {
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
            if (CharSequenceUtil.isNotBlank(entity.getRequestParam())) {
                String str = HtmlUtil.unescape(entity.getRequestParam());
                entity.setRequestParam(str);
            }
            if (CharSequenceUtil.isNotBlank(entity.getRequestHeaders())) {
                String str = HtmlUtil.unescape(entity.getRequestHeaders());
                entity.setRequestHeaders(str);
            }
            if (CharSequenceUtil.isNotBlank(entity.getRequestBody())) {
                String str = HtmlUtil.unescape(entity.getRequestBody());
                entity.setRequestBody(str);
            }
            if (CharSequenceUtil.isNotBlank(entity.getResponseHeaders())) {
                String str = HtmlUtil.unescape(entity.getResponseHeaders());
                entity.setResponseHeaders(str);
            }
            if (CharSequenceUtil.isNotBlank(entity.getResponseBody())) {
                String str = HtmlUtil.unescape(entity.getResponseBody());
                entity.setResponseBody(str);
            }
        });
        final List<GatewayLogDO> models = BeanUtil.copyToList(entities, GatewayLogDO.class);
        super.saveBatch(models);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<String> ids) {
        List<GatewayLogDO> data = super.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return super.removeByIds(ids, true);
    }

    @Override
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
        final IPage<GatewayLogDO> modelPage = super.page(page, wrapper);
        final IPage<GatewayLogPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, GatewayLogPageVO.class));
        List<GatewayLogPageVO> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            this.buildUserInfo(result, records);
        }
        return result;
    }

    @Override
    public EsPageDTO<GatewayLogPageVO> pageByAfter(GatewayLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<GatewayLogDO> wrapper = buildPageWrapper(entity);

        EsPageDTO<GatewayLogDO> page = EsPageDTO.<GatewayLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<GatewayLogDO> resultPage = super.pageByAfter(page, wrapper);

        List<GatewayLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = GatewayLogServiceImpl.enrichUserInfo(
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
            .records(voList)
            .searchAfter(resultPage.getSearchAfter())
            .build();
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public GatewayLogExtendVO getDetail(String id) {
        GatewayLogDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        GatewayLogExtendVO result = BeanUtil.copyProperties(data, GatewayLogExtendVO.class);
        result.setMobile(GatewayLogServiceImpl.getMobileByUserId(userClient, result.getUserId()));
        this.buildLocation(result);
        return result;
    }

    private void buildUserAgent(GatewayLogDTO entity) {
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

    private void buildDevice(GatewayLogDTO entity) {
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

    private void buildLocation(GatewayLogDTO entity) {
        if (CharSequenceUtil.isNotBlank(entity.getProvince())
            && CharSequenceUtil.isNotBlank(entity.getCity())) {
            return;
        }
        this.findAddress(entity.getIp()).ifPresent(address -> {
            entity.setProvince(address.getProvince());
            entity.setCity(address.getCity());
        });
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
            final GatewayLogDO cached = super.getOne(wrapper);
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

    private void buildUserInfo(IPage<GatewayLogPageVO> result, List<GatewayLogPageVO> records) {
        List<GatewayLogPageVO> enriched = GatewayLogServiceImpl.enrichUserInfo(userClient, records,
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
        List<UserVO> userList = GatewayLogServiceImpl.getUserList(userClient, ids);
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
        final List<UserVO> userList = GatewayLogServiceImpl.getUserList(userClient, Arrays.asList(userId));
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
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
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
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
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
