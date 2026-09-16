/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.dto.AkSkDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.domain.dto.StsTokenDTO;
import java.time.Instant;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;

/**
 * 阿里云业务层基础抽象类.
 *
 * @param <T> 配置实体类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractAliyunBaseServiceImpl<T> extends AbstractBaseServiceImpl<T> {

    public AbstractAliyunBaseServiceImpl(MultipartProperties multipartProperties) {
        super(multipartProperties);
    }

    /**
     * 获取 AssumeRoleResponse
     *
     * @param region 区域
     * @param akSk   阿里云配置
     * @param sts    临时访问凭证配置
     * @return DefaultAcsClient
     */
    protected DefaultAcsClient initAcsClient(String region, AkSkDTO akSk, StsTokenDTO sts) {
        DefaultProfile profile;
        if (Objects.nonNull(sts) && Boolean.TRUE.equals(sts.getEnabled())) {
            refreshStsTokenIfNeeded(akSk, sts);
            profile = DefaultProfile.getProfile(region, sts.getAccessKey(), sts.getSecretKey(), sts.getSecurityToken());
        } else {
            profile = DefaultProfile.getProfile(region, akSk.getAccessKey(), akSk.getSecretKey());
        }
        return new DefaultAcsClient(profile);
    }

    /**
     * 刷新临时访问凭证.
     *
     * @param akSk 阿里云配置
     * @param sts  临时访问凭证配置
     */
    protected synchronized void refreshStsTokenIfNeeded(AkSkDTO akSk, StsTokenDTO sts) {
        final Long securityTokenExpiration = sts.getExpiration();
        if (Objects.isNull(securityTokenExpiration) || System.currentTimeMillis() > securityTokenExpiration) {
            AssumeRoleResponse response = this.getAssumeRoleResponse(akSk, sts);
            final long expiration = Instant.parse(response.getCredentials().getExpiration()).toEpochMilli();
            sts.setAccessKey(response.getCredentials().getAccessKeyId());
            sts.setSecretKey(response.getCredentials().getAccessKeySecret());
            sts.setSecurityToken(response.getCredentials().getSecurityToken());
            sts.setExpiration(expiration);
        }
    }

    /**
     * 获取临时访问凭证.
     *
     * @param akSk 阿里云配置
     * @param sts  临时访问凭证配置
     * @return AssumeRoleResponse
     */
    protected AssumeRoleResponse getAssumeRoleResponse(AkSkDTO akSk, StsTokenDTO sts) {
        DefaultProfile.addEndpoint(SymbolConstant.EMPTY_STR, "Sts", sts.getEndpoint());
        IClientProfile clientProfile = DefaultProfile.getProfile(SymbolConstant.EMPTY_STR, akSk.getAccessKey(), akSk.getSecretKey());
        DefaultAcsClient client = new DefaultAcsClient(clientProfile);
        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(sts.getRoleArn());
        request.setRoleSessionName("aliyun-java-sdk-core-" + System.currentTimeMillis());
        if (CharSequenceUtil.isNotBlank(sts.getPolicy())) {
            request.setPolicy(sts.getPolicy());
        }
        request.setDurationSeconds(OssConstant.SECURITY_TOKEN_EXPIRE_TIME);
        AssumeRoleResponse response;
        try {
            response = client.getAcsResponse(request);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.GET_ACCESS_CREDENTIALS_ERROR);
        } finally {
            this.closeAcsClient(client);
        }
        return response;
    }

    /**
     * 关闭AcsClient
     *
     * @param acsClient
     */
    protected void closeAcsClient(DefaultAcsClient acsClient) {
        if (Objects.nonNull(acsClient)) {
            acsClient.shutdown();
        }
    }
}
