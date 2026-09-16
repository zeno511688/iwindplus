/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 发送短信数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "发送短信数据传输对象")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendDTO implements Serializable {

    /**
     * 模板编码（必填）.
     */
    @Schema(description = "模板编码")
    private String tplCode;

    /**
     * 手机集合（必填）.
     */
    @Schema(description = "手机集合")
    @NotEmpty(message = "{phoneNumbers.notEmpty}")
    private List<String> phoneNumbers;

    /**
     * 模板参数，用于替换短信模板中的参数（可选）.
     */
    @Schema(description = "模板参数，用于替换短信模板中的参数")
    private List<String> templateParams;

    /**
     * 上行短信扩展码，上行短信，指发送给通信服务提供商的短信，用于定制某种服务、完成查询，或是办理某种业务等，需要收费的，按运营商普通短信资费进行扣费。（可选）.
     */
    @Schema(description = "上行短信扩展码")
    private String smsUpExtendCode;
}
