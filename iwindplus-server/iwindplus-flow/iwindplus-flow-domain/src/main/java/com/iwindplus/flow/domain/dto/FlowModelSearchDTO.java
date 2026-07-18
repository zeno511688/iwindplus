/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.flow.domain.enums.FlowModelStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程模型搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "流程模型搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelSearchDTO extends DbPageDTO {

    /**
     * 状态（TO_BE_PUBLISHED：待发布，PUBLISHED：已发布，DISABLED：已停用）.
     */
    @Schema(description = "状态（TO_BE_PUBLISHED：待发布，PUBLISHED：已发布，DISABLED：已停用）")
    private FlowModelStatusEnum status;

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
     * 分类主键.
     */
    @Schema(description = "分类主键")
    private Long categoryId;

    /**
     * 分类编码.
     */
    @Schema(description = "分类编码")
    private String categoryCode;

    /**
     * 分类名称.
     */
    @Schema(description = "分类名称")
    private String categoryName;
}
