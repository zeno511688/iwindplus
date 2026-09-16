/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.ocr.service.BaseConfigService;
import com.iwindplus.base.util.IosUtil;
import com.iwindplus.base.util.domain.enums.ImageTypeEnum;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR业务层基础配置抽象类.
 *
 * @param <T> 配置实体类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBaseConfigServiceImpl<T> implements BaseConfigService<T> {

    private final MultipartProperties multipartProperties;

    /**
     * 配置.
     */
    protected T config;

    @Override
    public T getConfig() {
        return this.config;
    }

    @Override
    public void setConfig(T config) {
        this.config = config;
    }

    /**
     * 校验文件.
     *
     * @param file 文件
     */
    protected void checkFile(MultipartFile file) {
        if (Objects.isNull(file)) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        String suffix = FileNameUtil.getSuffix(file.getOriginalFilename());
        if (CharSequenceUtil.isNotBlank(suffix) && Stream.of(ImageTypeEnum.values()).noneMatch(m -> Objects.equals(m.name(), suffix))) {
            throw new BizException(BizCodeEnum.FILE_IS_NOT_IMAGE, new Object[]{suffix});
        }
        final long fileSize = file.getSize();
        final long maxFileSize = Optional.ofNullable(this.multipartProperties)
            .map(MultipartProperties::getMaxFileSize)
            .map(DataSize::toBytes)
            .orElse(DataSize.ofMegabytes(1).toBytes());
        if (fileSize > maxFileSize) {
            throw new BizException(BizCodeEnum.FILE_TOO_BIG, new Object[]{fileSize});
        }
    }

    /**
     * 关闭Response.
     *
     * @param response 响应
     */
    protected void closeResponse(Response response) {
        IosUtil.closeQuietly(response);
    }
}
