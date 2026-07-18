/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.util.AddressUtil;
import com.iwindplus.base.util.domain.vo.AddressVO;
import com.iwindplus.log.domain.dto.OperationLogDTO;
import com.iwindplus.log.domain.dto.OperationLogNewestDTO;
import com.iwindplus.log.domain.dto.OperationLogSearchDTO;
import com.iwindplus.log.domain.vo.OperationLogExtendVO;
import com.iwindplus.log.domain.vo.OperationLogPageVO;
import com.iwindplus.log.server.dal.model.OperationLogDO;
import com.iwindplus.log.server.service.OperationLogService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import java.util.ArrayList;
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

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(OperationLogDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getIp())) {
            entity.setIp(MDC.get(HeaderConstant.REAL_IP));
        }
        if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
            entity.setBizTraceId(MDC.get(HeaderConstant.TRACE_ID));
        }
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(IdUtil.simpleUUID());
        }
        this.buildSystemInfo(entity);
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
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = new EsLambdaQueryWrapper<>();
        final PageDTO<OperationLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
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
        final EsLambdaQueryWrapper<OperationLogDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(OperationLogDO::getOrgId, entity.getOrgId())
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

    private void buildSystemInfo(OperationLogDTO entity) {
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

    private void buildLocation(OperationLogExtendVO entity) {
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

    private void buildUserInfo(IPage<OperationLogPageVO> result, List<OperationLogPageVO> records) {
        final List<Long> ids = records.stream().filter(Objects::nonNull).map(OperationLogPageVO::getUserId).toList();
        if (CollUtil.isNotEmpty(ids)) {
            List<UserVO> userList = GatewayLogServiceImpl.getUserList(userClient, ids);
            if (CollUtil.isNotEmpty(userList)) {
                Map<Long, UserVO> userMap = userList.stream()
                    .filter(Objects::nonNull).collect(Collectors.toMap(UserVO::getId, Function.identity()));
                final List<OperationLogPageVO> list = records.stream()
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
}
