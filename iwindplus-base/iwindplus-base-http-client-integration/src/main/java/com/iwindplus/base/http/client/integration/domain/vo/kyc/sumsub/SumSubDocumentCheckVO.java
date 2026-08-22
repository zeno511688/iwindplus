/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SumSub 文档检查结果.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubDocumentCheckVO implements Serializable {

    /**
     * 检查ID.
     */
    private String id;

    /**
     * 检查类型.
     */
    private String checkType;

    /**
     * 检查结果状态.
     */
    private String status;

    /**
     * 检查结果.
     */
    private String result;

    /**
     * 检查详情.
     */
    private List<CheckDetail> details;

    /**
     * 检查详情.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckDetail implements Serializable {

        /**
         * 字段名.
         */
        private String field;

        /**
         * 字段值.
         */
        private String value;

        /**
         * 状态.
         */
        private String status;

        /**
         * 描述.
         */
        private String description;
    }
}
