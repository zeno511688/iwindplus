/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 数据库分页视图对象.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库分页视图对象")
@Data
@SuperBuilder
@AllArgsConstructor
public class DbPageVO<T> implements Serializable {

    /**
     * 当前页.
     */
    @Schema(description = "当前页")
    private Integer current;

    /**
     * 每页显示条数.
     */
    @Schema(description = "每页显示条数")
    private Integer size;

    /**
     * 总条数.
     */
    @Schema(description = "总条数")
    private Integer total;

    /**
     * 总页数.
     */
    @Schema(description = "总页数")
    private Integer pages;

    /**
     * 当前页数据列表.
     */
    @Schema(description = "当前页数据列表")
    private List<T> records;

    /**
     * 构造方法.
     *
     * @param current 当前页
     * @param size    每页显示条数
     * @param total   总条数
     * @param records 当前页数据列表
     */
    public DbPageVO(Integer current, Integer size, Integer total, List<T> records) {
        this.current = current <= 0 ? 1 : current;
        this.size = size <= 0 ? 10 : size;
        this.total = total < 0 ? 0 : total;
        this.pages = (this.total + this.size - 1) / this.size;
        this.records = records;
    }
}
