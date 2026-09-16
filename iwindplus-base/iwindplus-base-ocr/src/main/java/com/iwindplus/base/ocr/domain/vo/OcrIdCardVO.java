/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 身份证识别结果.
 *
 * @author zengdegui
 * @since 2026/9/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrIdCardVO {

    /**
     * 姓名.
     */
    private String name;

    /**
     * 性别.
     */
    private String sex;

    /**
     * 民族.
     */
    private String nation;

    /**
     * 出生日期.
     */
    private String birth;

    /**
     * 住址.
     */
    private String address;

    /**
     * 身份证号.
     */
    private String idNumber;

    /**
     * 签发机关.
     */
    private String authority;

    /**
     * 有效期限.
     */
    private String validPeriod;
}
