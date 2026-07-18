/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 第三方绑定授权保存或修改数据传输对象.
 *
 * @author zengdegui
 * @since 2019/8/23
 */
@Schema(description = "第三方绑定授权保存或修改数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdBindGrantSaveEditDTO extends ThirdBindGrantDTO {

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;
}
