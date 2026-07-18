/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.ocr.domain.enums.OcrXiangyunIdTypeEnum;
import org.springframework.web.multipart.MultipartFile;

/**
 * 翔云ocr业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface OcrXiangyunService extends OcrBaseService {
    /**
     * 识别身份证图片的内容.
     *
     * @param file   文件
     * @param idType 证件类型
     * @return JsonNode
     */
    JsonNode parseIdCardImage(MultipartFile file, OcrXiangyunIdTypeEnum idType);

    /**
     * 识别营业执照图片的内容.
     *
     * @param file 文件
     * @return JsonNode
     */
    JsonNode parseBusinessLicenseImage(MultipartFile file);
}
