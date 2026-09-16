/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * IP黑名单变化视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "IP黑名单变化视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IpBlackListChangeVO implements Serializable {

    /**
     * 新IP.
     */
    @Schema(description = "新IP")
    private List<String> newIp;

    /**
     * 旧IP.
     */
    @Schema(description = "旧IP")
    private List<String> oldIp;
}
