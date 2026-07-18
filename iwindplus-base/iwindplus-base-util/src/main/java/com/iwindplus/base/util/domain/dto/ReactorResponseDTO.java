/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Reactor响应数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "Reactor响应数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReactorResponseDTO implements Serializable {

    /**
     * 响应状态.
     */
    @Schema(description = "响应状态")
    private Integer responseStatus;

    /**
     * 响应头.
     */
    @Schema(description = "响应头")
    private Map<String, String> responseHeaders;

    /**
     * 响应体.
     */
    @Schema(description = "响应体")
    private String responseBody;
}
