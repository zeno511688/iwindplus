/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.util.AddressUtil;
import com.iwindplus.base.util.CheckDataUtil;
import com.iwindplus.base.util.domain.vo.AddressVO;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.domain.dto.GatewayLogSearchDTO;
import com.iwindplus.log.domain.vo.GatewayLogExtendVO;
import com.iwindplus.log.domain.vo.GatewayLogPageVO;
import com.iwindplus.log.server.dal.model.GatewayLogDO;
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

    @CacheEvict(allEntries = true)
    @Override
    public boolean saveBatch(List<GatewayLogDTO> entities) {
        CheckDataUtil.checkBatchOperationSize(entities.size(), NumberConstant.NUMBER_FIFTY);
        entities.forEach(entity -> {
            if (CharSequenceUtil.isBlank(entity.getIp())) {
                entity.setIp(MDC.get(HeaderConstant.REAL_IP));
            }
            if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
                entity.setBizTraceId(MDC.get(HeaderConstant.TRACE_ID));
            }
            this.buildSystemInfo(entity);
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
        final EsLambdaQueryWrapper<GatewayLogDO> wrapper = new EsLambdaQueryWrapper<>();
        final PageDTO<GatewayLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
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

    private void buildSystemInfo(GatewayLogDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getPlatformName())
            && CharSequenceUtil.isBlank(entity.getOsName())
            && CharSequenceUtil.isBlank(entity.getBrowserName())) {
            final String userAgentStr = Optional.ofNullable(HeaderContextHolder.getContext())
                .map(m -> m.get(HttpHeaders.USER_AGENT)).orElse(null);
            if (CharSequenceUtil.isNotBlank(userAgentStr)) {
                final UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
                entity.setPlatformName(Optional.ofNullable(userAgent).map(UserAgent::getPlatform).map(Platform::getName).orElse(null));
                entity.setOsName(Optional.ofNullable(userAgent).map(UserAgent::getOs).map(OS::getName).orElse(null));
                entity.setBrowserName(Optional.ofNullable(userAgent).map(UserAgent::getBrowser).map(Browser::getName).orElse(null));
            }
        }
    }

    private void buildLocation(GatewayLogExtendVO entity) {
        if (CharSequenceUtil.isBlank(entity.getIp())) {
            return;
        }
        try {
            AddressVO result = AddressUtil.getAddress(entity.getIp());
            entity.setProvince(Optional.ofNullable(result).map(AddressVO::getProvince).orElse(null));
            entity.setCity(Optional.ofNullable(result).map(AddressVO::getCity).orElse(null));
        } catch (Exception e) {
            log.error("获取地址信息异常", e);
        }
    }

    private void buildUserInfo(IPage<GatewayLogPageVO> result, List<GatewayLogPageVO> records) {
        final List<Long> ids = records.stream().filter(Objects::nonNull).map(GatewayLogPageVO::getUserId).toList();
        if (CollUtil.isNotEmpty(ids)) {
            List<UserVO> userList = GatewayLogServiceImpl.getUserList(userClient, ids);
            if (CollUtil.isNotEmpty(userList)) {
                Map<Long, UserVO> userMap = userList.stream()
                    .filter(Objects::nonNull).collect(Collectors.toMap(UserVO::getId, Function.identity()));
                final List<GatewayLogPageVO> list = records.stream()
                    .filter(Objects::nonNull)
                    .peek(m -> {
                        UserVO u = userMap.get(m.getUserId());
                        if (u != null) {
                            m.setJobNumber(u.getJobNumber());
                            m.setMobile(u.getMobile());
                        }
                    }).toList();
                result.setRecords(list);
            }
        }
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
}
