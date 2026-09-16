/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.oss.domain.dto.OssCloudUploadDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.service.BaseService;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.HttpsUtil;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储业务层基础抽象类.
 *
 * @param <T> 配置实体类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractOssBaseServiceImpl<T extends OssProperty.BaseConfig>
    extends AbstractAliyunBaseServiceImpl<T> implements BaseService {

    public AbstractOssBaseServiceImpl(MultipartProperties multipartProperties) {
        super(multipartProperties);
    }

    /**
     * 健康检查.
     *
     * @return 是否健康
     */
    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getPriority() {
        return Optional.ofNullable(this.getConfig().getPriority()).orElse(Integer.MAX_VALUE);
    }

    /**
     * 获取配置编码.
     *
     * @return 配置编码
     */
    @Override
    public String getCode() {
        return this.getConfig().getCode();
    }

    /**
     * 准备上传内容（文件、字节数组、外网URL 三选一，统一转换为字节数组）.
     *
     * @param request 上传请求参数
     */
    protected void prepareUpload(OssCloudUploadDTO request) {
        final MultipartFile file = request.getFile();
        final byte[] data = request.getData();
        final String url = request.getUrl();
        final String contentType = request.getContentType();

        Assert.isTrue(
            Objects.nonNull(file) || ArrayUtil.isNotEmpty(data) || CharSequenceUtil.isNotBlank(url),
            "文件内容不能为空"
        );

        if (ArrayUtil.isEmpty(data) && Objects.nonNull(file)) {
            request.setData(FilesUtil.getBytes(file));
            request.setContentType(file.getContentType());

            if (CharSequenceUtil.isBlank(request.getSourceFileName())) {
                request.setSourceFileName(file.getOriginalFilename());
            }
        } else if (ArrayUtil.isEmpty(data) && CharSequenceUtil.isNotBlank(url)) {
            final byte[] bytes = HttpsUtil.downloadBytes(url);
            request.setData(bytes);
            request.setContentType(Optional.ofNullable(contentType).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));

            if (CharSequenceUtil.isBlank(request.getSourceFileName())) {
                request.setSourceFileName(super.getNewFileName(url, null));
            }
        }

        super.checkFile(request.getData());
    }
}
