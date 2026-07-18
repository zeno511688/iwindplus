/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;


import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 国际化消息条件查询数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "国际化消息条件查询数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class I18nMsgQueryDTO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 消息状态.
     */
    @Schema(description = "消息状态")
    private EnableStatusEnum msgStatus;

    /**
     * 项目主键.
     */
    @Schema(description = "项目主键")
    private Long projectId;

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
}
