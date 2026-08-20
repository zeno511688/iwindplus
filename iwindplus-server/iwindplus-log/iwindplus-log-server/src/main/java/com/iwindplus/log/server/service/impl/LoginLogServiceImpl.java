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
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.http.client.integration.service.AddressService;
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
                    entity.setIp(MDC.get(HeaderConstant.X_REAL_IP));
                }
                if (CharSequenceUtil.isBlank(entity.getBizTraceId())) {
                    entity.setBizTraceId(MDC.get(HeaderConstant.X_TRACE_ID));
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
            this.addressService.getAddressByPconline(entity.getIp())
                .ifPresent(address -> {
                    entity.setProvince(address.getProvince());
                    entity.setCity(address.getCity());
                });
        } catch (Exception e) {
            log.error("获取地址信息异常", e);
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

}
