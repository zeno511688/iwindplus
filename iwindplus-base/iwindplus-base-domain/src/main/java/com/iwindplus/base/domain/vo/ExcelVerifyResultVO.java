/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * excel校验结果视图对象.
 *
 * @author zengdegui
 * @since 2024/06/30 18:16
 */
@Schema(description = "excel校验结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelVerifyResultVO implements Serializable {

    /**
     * 是否正确.
     */
    private Boolean success;

    /**
     * 错误信息.
     */
    private String msg;
}
