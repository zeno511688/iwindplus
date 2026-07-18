/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 客户端基础视图对象.
 *
 * @author zengdegui
 * @since 2019/2/15
 */
@Schema(description = "客户端基础视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientBaseVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 客户端id.
     */
    @Schema(description = "客户端id")
    private String clientId;

    /**
     * 客户端名称.
     */
    @Schema(description = "客户端名称")
    private String clientName;

    /**
     * 客户端密钥.
     */
    @Schema(description = "客户端密钥")
    private String clientSecret;
}
