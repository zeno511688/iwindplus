/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 国际化项目视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "国际化项目视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class I18nProjectVO extends DbVersionBaseVO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 平台类型（MGT：后台管理端，WEB：web端）.
     */
    @Schema(description = "平台类型（MGT：后台管理端，WEB：web端）")
    private PlatformTypeEnum platformType;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    private String fileName;

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
