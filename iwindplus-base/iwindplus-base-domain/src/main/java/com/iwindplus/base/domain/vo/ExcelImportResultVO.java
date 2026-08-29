/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel导入结果.
 *
 * @param <T> 数据类型
 * @author zengdegui
 * @since 2024/06/30 18:03
 */
@Schema(description = "Excel导入结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultVO<T extends ExcelImportResultBaseVO> {

    /**
     * 所有数据.
     */
    @Schema(description = "所有数据")
    private List<T> list;

    /**
     * 正确数据.
     */
    @Schema(description = "正确数据")
    private List<T> rightList;

    /**
     * 失败数据.
     */
    @Schema(description = "失败数据")
    private List<T> failList;
}
