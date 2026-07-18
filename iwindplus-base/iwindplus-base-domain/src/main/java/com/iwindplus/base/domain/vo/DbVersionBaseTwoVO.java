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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 数据库乐观锁视图对象（主键为字符串）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库乐观锁视图对象（主键为字符串）")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DbVersionBaseTwoVO extends DbBaseTwoVO {

    /**
     * 乐观锁.
     */
    @Schema(description = "乐观锁")
    private Integer version;
}
