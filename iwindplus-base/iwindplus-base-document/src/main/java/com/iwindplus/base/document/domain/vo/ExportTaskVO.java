/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.vo;

import com.iwindplus.base.document.domain.dto.ExportTaskExtDTO;
import com.iwindplus.base.document.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 导出任务VO.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Schema(description = "导出任务VO")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskVO extends DbVersionBaseVO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private ExportTaskStatusEnum status;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件路径.
     */
    @Schema(description = "文件路径")
    private String filePath;

    /**
     * 执行器名称.
     */
    @Schema(description = "执行器名称")
    private String executeName;

    /**
     * 查询参数.
     */
    @Schema(description = "查询参数")
    private Map<String, Object> queryParam;

    /**
     * 导出数据总数.
     */
    @Schema(description = "导出数据总数")
    private Long totalCount;

    /**
     * 已导出数量.
     */
    @Schema(description = "已导出数量")
    private Long exportedCount;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private Long expireTime;

    /**
     * 下一次重试时间.
     */
    @Schema(description = "下一次重试时间")
    private Long nextRetryTime;

    /**
     * 重试次数.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 累计耗时（毫秒）.
     */
    @Schema(description = "累计耗时（毫秒）")
    private Long costTime;

    /**
     * 进度比例（0-100）.
     */
    @Schema(description = "进度比例（0-100）")
    private Integer progress;

    /**
     * 扩展对象.
     */
    @Schema(description = "扩展对象")
    private ExportTaskExtDTO ext;
}
