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
import com.iwindplus.base.ocr.domain.property.OcrProperty;
import com.iwindplus.base.ocr.service.OcrBaseConfigService;
import com.iwindplus.base.util.domain.enums.ImageTypeEnum;
import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.web.multipart.MultipartFile;

/**
 * ocr业务层基础配置抽象类.
 *
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractOcrBaseConfigServiceImpl implements OcrBaseConfigService {
    @Resource
    private MultipartProperties multipartProperties;

    @Resource
    private OcrProperty property;

    @Override
    public OcrProperty getConfig() {
        return this.property;
    }

    @Override
    public void setConfig(OcrProperty config) {
        this.property = config;
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
        long maxFileSize = this.multipartProperties.getMaxFileSize().toBytes();
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
        if (Objects.nonNull(response)) {
            response.close();
        }
    }
}
