/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 谷歌验证器信息对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "谷歌验证器信息对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthVO implements Serializable {

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String key;

    /**
     * 内容.
     */
    @Schema(description = "内容")
    private String content;
}
