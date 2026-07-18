/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库分页数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库分页数据传输对象")
@Data
@SuperBuilder
@AllArgsConstructor
public class DbPageDTO implements Serializable {

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
     * 排序字段信息.
     */
    @Schema(description = "排序字段信息")
    private List<OrderItemDTO> orders;

    /**
     * 构造方法.
     */
    public DbPageDTO() {
        this(NumberConstant.NUMBER_ONE, NumberConstant.NUMBER_TEN);
    }

    /**
     * 构造方法.
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    public DbPageDTO(Integer current, Integer size) {
        this.current = current <= 0 ? NumberConstant.NUMBER_ONE : current;
        this.size = size <= 0 ? NumberConstant.NUMBER_TEN : size;
    }

    /**
     * 排序字段信息.
     *
     * @author zengdegui
     * @since 2020/4/15
     */
    @Schema(description = "排序字段信息")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO implements Serializable {

        /**
         * 列.
         */
        @Schema(description = "列")
        private String column;

        /**
         * 是否升序.
         */
        @Schema(description = "是否升序")
        private Boolean asc;

        /**
         * 升序.
         *
         * @param column 列
         * @return OrderItemDTO
         */
        public static OrderItemDTO asc(String column) {
            return build(column, Boolean.TRUE);
        }

        /**
         * 降序.
         *
         * @param column 列
         * @return OrderItemDTO
         */
        public static OrderItemDTO desc(String column) {
            return build(column, Boolean.FALSE);
        }

        /**
         * 批量升序.
         *
         * @param columns 列集合
         * @return List<OrderItemDTO>
         */
        public static List<OrderItemDTO> ascList(String... columns) {
            return Arrays.stream(columns).map(OrderItemDTO::asc).collect(Collectors.toList());
        }

        /**
         * 批量降序.
         *
         * @param columns 列集合
         * @return List<OrderItemDTO>
         */
        public static List<OrderItemDTO> descList(String... columns) {
            return Arrays.stream(columns).map(OrderItemDTO::desc).collect(Collectors.toList());
        }

        private static OrderItemDTO build(String column, Boolean asc) {
            return OrderItemDTO
                .builder()
                .column(column)
                .asc(asc)
                .build();
        }
    }
}
