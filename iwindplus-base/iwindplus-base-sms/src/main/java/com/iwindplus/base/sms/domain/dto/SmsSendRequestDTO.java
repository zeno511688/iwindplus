/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 短信发送参数.
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendRequestDTO extends SmsSendBaseDTO {

    /**
     * 手机号集合（必填）.
     */
    private List<String> phoneNumbers;

    /**
     * 短信签名（可选，部分服务商需要，如阿里云）.
     */
    private String signName;

    /**
     * 模板参数，用于替换短信模板中的参数（可选）.
     */
    private List<String> templateParams;

    /**
     * 每个分组的手机个数（可选，默认：100）.
     */
    private Integer phoneNumberGroupSize;
}
