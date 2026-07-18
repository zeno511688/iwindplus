/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.es.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * es分页数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "es分页数据传输对象")
@Data
@SuperBuilder
@AllArgsConstructor
public class EsPageDTO<T> implements Serializable {

    /**
     * 每页显示条数.
     */
    @Schema(description = "每页显示条数")
    private Integer size;

    /**
     * 总数.
     */
    @Schema(description = "总数")
    private Long total;

    /**
     * 记录.
     */
    @Schema(description = "记录")
    private List<T> records;

    /**
     * 下一页游标。
     */
    @Schema(description = "下一页游标")
    private List<Object> searchAfter;
}
