/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * IP黑名单数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "IP黑名单数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IpBlackListDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * ip.
     */
    @Schema(description = "ip")
    @NotBlank(message = "{ip.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{ip.length}", groups = {SaveGroup.class, EditGroup.class})
    private String ip;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;
}
