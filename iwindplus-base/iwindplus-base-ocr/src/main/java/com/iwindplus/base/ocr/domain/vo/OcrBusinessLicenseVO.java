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
 * 营业执照ocr识别结果.
 *
 * @author zengdegui
 * @since 2026/9/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrBusinessLicenseVO {

    /**
     * 公司名称.
     */
    private String companyName;

    /**
     * 统一社会信用代码.
     */
    private String creditCode;

    /**
     * 法定代表人.
     */
    private String legalPerson;

    /**
     * 注册地址.
     */
    private String address;

    /**
     * 经营范围.
     */
    private String businessScope;

    /**
     * 成立日期.
     */
    private String establishDate;

    /**
     * 营业期限.
     */
    private String businessTerm;

    /**
     * 登记机关.
     */
    private String registrationAuthority;
}
