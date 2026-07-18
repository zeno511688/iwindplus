/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 短信批量发送结果视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信批量发送结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsBatchVO implements Serializable {

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 手机号集合.
     */
    @Schema(description = "手机号集合")
    private List<String> phoneNumbers;

    /**
     * 模板参数，用于替换短信模板中的参数.
     */
    @Schema(description = "模板参数，用于替换短信模板中的参数")
    private List<String> templateParams;

    /**
     * 每个分组的手机个数.
     */
    @Schema(description = "每个分组的手机个数")
    private Integer phoneNumberGroupSize;

}