/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信水印数据视图对象.
 *
 * @author zengdegui
 * @since 2026/09/12 19:40
 */
@Schema(description = "微信水印数据视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WxWatermarkVO implements Serializable {

    /**
     * 时间戳.
     */
    @Schema(description = "时间戳")
    private String timestamp;

    /**
     * appid.
     */
    @Schema(description = "appid")
    private String appid;
}
