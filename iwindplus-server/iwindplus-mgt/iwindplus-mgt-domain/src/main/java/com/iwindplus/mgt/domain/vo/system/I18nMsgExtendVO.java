/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 国际化消息扩展视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "国际化消息扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class I18nMsgExtendVO extends I18nMsgVO {

    /**
     * 项目状态.
     */
    @Schema(description = "项目状态")
    private EnableStatusEnum projectStatus;

    /**
     * 项目平台类型.
     */
    @Schema(description = "项目平台类型")
    private PlatformTypeEnum projectPlatformType;

    /**
     * 项目编码.
     */
    @Schema(description = "项目编码")
    private String projectCode;

    /**
     * 项目名称.
     */
    @Schema(description = "项目名称")
    private String projectName;

    /**
     * 项目文件名称.
     */
    @Schema(description = "项目文件名称")
    private String projectFileName;
}
