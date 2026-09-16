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
import com.iwindplus.auth.common.enums.AuthModuleEnum;
import com.iwindplus.base.address.domain.vo.AddressVO;
import com.iwindplus.base.address.factory.AddressExecuteHandlerFactory;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.log.application.query.dto.LoginLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.LoginLogSearchDTO;
import com.iwindplus.log.application.query.vo.LoginLogExtendVO;
import com.iwindplus.log.application.query.vo.LoginLogPageVO;
import com.iwindplus.log.application.query.vo.LoginLogVO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.LoginLogDO;
import com.iwindplus.log.infrastructure.persistence.log.LoginLogRepository;
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
 * 登陆日志查询业务层.
 *
 * @author zengdegui
 * @since 2026/09/15 08:52
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_LOGIN_LOG})
@RequiredArgsConstructor
public class LoginLogQueryService {

    private final LoginLogRepository loginLogRepository;
    private final UserClient userClient;
    private final AddressExecuteHandlerFactory addressExecuteHandlerFactory;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<LoginLogPageVO>
     */
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
        final IPage<LoginLogDO> modelPage = this.loginLogRepository.page(page, wrapper);
        final IPage<LoginLogPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, LoginLogPageVO.class));
        List<LoginLogPageVO> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            this.buildUserInfo(result, records);
        }
        return result;
    }

    /**
     * 深分页列表.
     *
     * @param entity 对象
     * @return EsPageDTO<LoginLogPageVO>
     */
    public EsPageDTO<LoginLogPageVO> pageByAfter(LoginLogSearchAfterDTO entity) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = this.buildPageWrapper(entity);

        EsPageDTO<LoginLogDO> page = EsPageDTO.<LoginLogDO>builder()
            .size(entity.getSize() == null ? 10 : entity.getSize())
            .searchAfter(entity.getSearchAfter())
            .build();

        EsPageDTO<LoginLogDO> resultPage = this.loginLogRepository.pageByAfter(page, wrapper);

        List<LoginLogPageVO> voList = null;
        if (CollUtil.isNotEmpty(resultPage.getRecords())) {
            voList = GatewayLogQueryService.enrichUserInfo(
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
            .pages(resultPage.getPages())
            .records(voList)
            .searchAfter(resultPage.getSearchAfter())
            .build();
    }

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return LoginLogExtendVO
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public LoginLogExtendVO getDetail(String id) {
        LoginLogDO data = this.loginLogRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        LoginLogExtendVO result = BeanUtil.copyProperties(data, LoginLogExtendVO.class);
        result.setMobile(GatewayLogQueryService.getMobileByUserId(userClient, result.getUserId()));
        this.buildLocation(result);
        return result;
    }

    /**
     * 获取最新登录信息.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return LoginLogVO
     */
    public LoginLogVO getLoginInfo(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<LoginLogDO> wrapper = EsWrappers.<LoginLogDO>lambdaQuery()
            .eq(LoginLogDO::getOrgId, orgId)
            .eq(LoginLogDO::getUserId, userId)
            .eq(LoginLogDO::getModuleName, AuthModuleEnum.LOGIN.getValue())
            .orderByDesc(LoginLogDO::getModifiedTimestamp)
            .limit(1);
        LoginLogDO data = this.loginLogRepository.getOne(wrapper);
        if (Objects.isNull(data)) {
            return new LoginLogVO();
        }

        return BeanUtil.copyProperties(data, LoginLogVO.class);
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
            final LoginLogDO cached = this.loginLogRepository.getOne(wrapper);
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

    private void buildUserInfo(IPage<LoginLogPageVO> result, List<LoginLogPageVO> records) {
        List<LoginLogPageVO> enriched = GatewayLogQueryService.enrichUserInfo(userClient, records,
            LoginLogPageVO::getUserId, v -> v::setJobNumber, v -> v::setMobile);
        result.setRecords(enriched);
    }

    private EsLambdaQueryWrapper<LoginLogDO> buildPageWrapper(LoginLogSearchAfterDTO entity) {
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
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
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
            Long userId = GatewayLogQueryService.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogQueryService.getUserIdByMobile(userClient, entity.getMobile());
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
