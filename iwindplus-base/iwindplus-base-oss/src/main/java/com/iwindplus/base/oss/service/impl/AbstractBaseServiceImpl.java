/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.dto.AkSkDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import com.iwindplus.base.util.DatesUtil;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用业务层抽象类.
 *
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractBaseServiceImpl {

    @Resource
    private MultipartProperties multipartProperties;

    /**
     * 获取服务器上传目录（应用所在的位置）.
     *
     * @return String
     */
    protected String getRootPath() {
        return Optional.ofNullable(this.multipartProperties.getLocation()).orElse(System.getProperty(FileConstant.USER_DIR));
    }

    /**
     * 获取相对路径（包含后缀）.
     *
     * @param prefix   存储目录前缀（必填）
     * @param fileName 文件名，包含文件后缀（必填）
     * @return String
     */
    protected String getRelativePath(String prefix, String fileName) {
        if (CharSequenceUtil.isBlank(prefix)) {
            throw new BizException(BizCodeEnum.FILE_DIR_EMPTY);
        }
        String suffix = FileUtil.getSuffix(fileName);
        if (CharSequenceUtil.isBlank(suffix)) {
            throw new BizException(BizCodeEnum.FILE_HAS_NOT_SUFFIX);
        }
        StringBuilder sb = new StringBuilder(prefix).append(SymbolConstant.SLASH)
            .append(DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN))
            .append(SymbolConstant.SLASH).append(fileName);
        return sb.toString();
    }

    /**
     * 重命名文件名（包含后缀）.
     *
     * @param path     路径（必填）
     * @param fileName 新文件名（必填）
     * @return String
     */
    protected String getNewFileName(String path, String fileName) {
        String filePath = StrUtil.subBefore(path, SymbolConstant.QUESTION_MARK, false);
        String suffix = FileUtil.getSuffix(filePath);
        if (CharSequenceUtil.isBlank(suffix)) {
            throw new BizException(BizCodeEnum.FILE_HAS_NOT_SUFFIX);
        }
        if (CharSequenceUtil.isBlank(fileName)) {
            return FileNameUtil.getName(filePath);
        }
        return new StringBuilder(FileUtil.getPrefix(fileName.trim())).append(SymbolConstant.POINT)
            .append(suffix).toString();
    }

    /**
     * 重命名文件名，文件名随机（包含后缀）.
     *
     * @param renamed        是否重命名文件名（必填）
     * @param sourceFileName 原文件，包含后缀（必填）
     * @return String
     */
    protected String getNewFileName(Boolean renamed, String sourceFileName) {
        if (Boolean.FALSE.equals(renamed)) {
            return sourceFileName.trim();
        }
        String suffix = FileUtil.getSuffix(sourceFileName);
        if (CharSequenceUtil.isBlank(suffix)) {
            throw new BizException(BizCodeEnum.FILE_HAS_NOT_SUFFIX);
        }
        return new StringBuilder(IdUtil.getSnowflakeNextIdStr()).append(SymbolConstant.POINT)
            .append(suffix).toString();
    }

    /**
     * 校验文件大小.
     *
     * @param file 文件
     */
    protected void checkFile(MultipartFile file) {
        long fileSize = file.getSize();
        long maxFileSize = this.multipartProperties.getMaxFileSize().toBytes();
        if (fileSize > maxFileSize) {
            throw new BizException(BizCodeEnum.FILE_TOO_BIG, new Object[]{fileSize});
        }
    }

    /**
     * 校验文件大小.
     *
     * @param data 字节数组
     */
    protected void checkFile(byte[] data) {
        long fileSize = data.length;
        long maxFileSize = this.multipartProperties.getMaxFileSize().toBytes();
        if (fileSize > maxFileSize) {
            throw new BizException(BizCodeEnum.FILE_TOO_BIG, new Object[]{fileSize});
        }
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
        if (Objects.nonNull(sts)) {
            final LocalDateTime securityTokenExpiration = sts.getExpiration();
            if (Objects.isNull(securityTokenExpiration) || LocalDateTime.now().isAfter(securityTokenExpiration)) {
                AssumeRoleResponse response = this.getAssumeRoleResponse(akSk, sts);
                final LocalDateTime expiration = DatesUtil.parseUtcDate(response.getCredentials().getExpiration());
                sts.setAccessKey(response.getCredentials().getAccessKeyId());
                sts.setSecretKey(response.getCredentials().getAccessKeySecret());
                sts.setSecurityToken(response.getCredentials().getSecurityToken());
                sts.setExpiration(expiration);
            }
            profile = DefaultProfile.getProfile(region, sts.getAccessKey(), sts.getSecretKey(), sts.getSecurityToken());
        } else {
            profile = DefaultProfile.getProfile(region, akSk.getAccessKey(), akSk.getSecretKey());
        }
        return new DefaultAcsClient(profile);
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
