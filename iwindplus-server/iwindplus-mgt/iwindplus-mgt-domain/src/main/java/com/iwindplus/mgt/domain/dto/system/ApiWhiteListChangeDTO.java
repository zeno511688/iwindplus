/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API白名单变化数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "API白名单变化数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiWhiteListChangeDTO implements Serializable {

    /**
     * 新API路径.
     */
    @Schema(description = "新API路径")
    private List<String> newApiUrl;

    /**
     * 旧API路径.
     */
    @Schema(description = "旧API路径")
    private List<String> oldApiUrl;
}
