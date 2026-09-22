/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.upms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 资源基础字段扩展视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "资源基础字段扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceBaseExtendVO extends ResourceBaseVO {

    /**
     * API路径集合.
     */
    @Schema(description = "API路径集合")
    private List<String> apiUrls;
}