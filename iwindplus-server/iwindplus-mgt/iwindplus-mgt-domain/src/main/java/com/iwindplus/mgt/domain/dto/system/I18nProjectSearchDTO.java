/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;


import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 国际化项目搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "国际化项目搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class I18nProjectSearchDTO extends DbPageDTO {

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
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    private String fileName;
}
