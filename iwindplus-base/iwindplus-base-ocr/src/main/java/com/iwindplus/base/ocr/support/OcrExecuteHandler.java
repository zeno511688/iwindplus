/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.support;

import com.iwindplus.base.ocr.domain.enums.OcrTypeEnum;
import com.iwindplus.base.ocr.domain.vo.OcrBusinessLicenseVO;
import com.iwindplus.base.ocr.domain.vo.OcrIdCardVO;
import com.iwindplus.base.ocr.service.BaseService;
import org.springframework.web.multipart.MultipartFile;

/**
 * OCR处理器接口.
 *
 * <p>策略模式：不同的OCR类型实现此接口。每个配置对应一个运行时策略实例。</p>
 *
 * @author zengdegui
 * @since 2026/9/1
 */
public interface OcrExecuteHandler extends BaseService {

    /**
     * 获取当前OCR类型.
     *
     * @return OcrTypeEnum
     */
    OcrTypeEnum getType();

    /**
     * 识别身份证图片的内容.
     *
     * @param file   文件
     * @param idType 证件类型（正面/背面）
     * @return OcrIdCardVO
     */
    OcrIdCardVO parseIdCardImage(MultipartFile file, Object idType);

    /**
     * 识别营业执照图片的内容.
     *
     * @param file 文件
     * @return OcrBusinessLicenseVO
     */
    OcrBusinessLicenseVO parseBusinessLicenseImage(MultipartFile file);
}
