/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */


package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.util.AddressUtil;
import com.iwindplus.base.util.domain.vo.AddressVO;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.domain.dto.LoginLogSearchDTO;
import com.iwindplus.log.domain.vo.LoginLogExtendVO;
import com.iwindplus.log.domain.vo.LoginLogPageVO;
import com.iwindplus.log.domain.vo.LoginLogVO;
import com.iwindplus.log.server.dal.model.LoginLogDO;
import com.iwindplus.log.server.service.LoginLogService;
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

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(LoginLogDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getIp())) {
            entity.setIp(MDC.get(HeaderConstant.REAL_IP));
        }
        if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
            entity.setBizTraceId(MDC.get(HeaderConstant.TRACE_ID));
        }
        this.buildSystemInfo(entity);
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
                    entity.setIp(MDC.get(HeaderConstant.REAL_IP));
                }
                if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
                    entity.setBizTraceId(MDC.get(HeaderConstant.TRACE_ID));
                }
                this.buildSystemInfo(entity);
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
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = new EsLambdaQueryWrapper<>();
        final PageDTO<LoginLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
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
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(LoginLogDO::getOrgId, orgId)
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

    private void buildSystemInfo(LoginLogDTO entity) {
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

    private void buildLocation(LoginLogExtendVO entity) {
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

    private void buildUserInfo(IPage<LoginLogPageVO> result, List<LoginLogPageVO> records) {
        final List<Long> ids = records.stream().filter(Objects::nonNull).map(LoginLogPageVO::getUserId).toList();
        if (CollUtil.isNotEmpty(ids)) {
            List<UserVO> userList = GatewayLogServiceImpl.getUserList(userClient, ids);
            if (CollUtil.isNotEmpty(userList)) {
                Map<Long, UserVO> userMap = userList.stream()
                    .filter(Objects::nonNull).collect(Collectors.toMap(UserVO::getId, Function.identity()));
                final List<LoginLogPageVO> list = records.stream()
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
