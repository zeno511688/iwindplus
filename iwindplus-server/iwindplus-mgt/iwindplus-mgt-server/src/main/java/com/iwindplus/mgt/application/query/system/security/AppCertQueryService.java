/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.security;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.mgt.application.query.system.security.dto.AppCertSearchDTO;
import com.iwindplus.mgt.application.query.system.security.vo.AppCertPageVO;
import com.iwindplus.mgt.application.query.system.security.vo.AppCertVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.security.AppCertDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.AppCertRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 应用凭证查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_APP_CERT})
@Slf4j
@RequiredArgsConstructor
public class AppCertQueryService {

    private final AppCertRepository appCertRepository;

    public IPage<AppCertPageVO> page(AppCertSearchDTO entity) {
        PageDTO<AppCertDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<AppCertDO> queryWrapper = Wrappers.lambdaQuery(AppCertDO.class)
            .orderByDesc(AppCertDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AppCertDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(AppCertDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey())) {
            queryWrapper.like(AppCertDO::getAccessKey, entity.getAccessKey().trim());
        }
        queryWrapper.select(AppCertDO::getId, AppCertDO::getCreatedTimestamp, AppCertDO::getCreatedBy,
            AppCertDO::getModifiedTimestamp, AppCertDO::getModifiedBy, AppCertDO::getVersion, AppCertDO::getStatus,
            AppCertDO::getName, AppCertDO::getAccessKey, AppCertDO::getTimeout, AppCertDO::getCertType, AppCertDO::getBuildInFlag
        );
        final PageDTO<AppCertDO> modelPage = this.appCertRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, AppCertPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public AppCertVO getDetail(Long id) {
        AppCertDO data = this.appCertRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, AppCertVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    public BaseSignVO getByAccessKey(String accessKey, AppCertTypeEnum appCertType) {
        AppCertDO data = this.appCertRepository.getOne(Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getAccessKey, accessKey)
            .eq(AppCertDO::getCertType, appCertType));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BaseSignVO.builder()
            .accessKey(data.getAccessKey())
            .secretKey(data.getSecretKey())
            .timeout(data.getTimeout().longValue())
            .build();
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public BaseSignVO getByCertType(AppCertTypeEnum appCertType) {
        AppCertDO data = this.appCertRepository.getOne(Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getCertType, appCertType));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BaseSignVO.builder()
            .accessKey(data.getAccessKey())
            .secretKey(data.getSecretKey())
            .timeout(data.getTimeout().longValue())
            .build();
    }

}
