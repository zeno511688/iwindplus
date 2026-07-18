/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 应用凭证数据视图对象.
 *
 * @author zengdegui
 * @since 2019/2/15
 */
@Schema(description = "应用凭证数据视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppCertDataVO implements Serializable {

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String secretKey;

    /**
     * 签名超时时间.
     */
    @Schema(description = "签名超时时间（单位：秒）")
    private Integer timeout;

    /**
     * 应用凭证类型.
     */
    @Schema(description = "应用凭证类型（API_SIGN：API签名，SERVICE_SIGN：服务签名）")
    private AppCertTypeEnum certType;
}
