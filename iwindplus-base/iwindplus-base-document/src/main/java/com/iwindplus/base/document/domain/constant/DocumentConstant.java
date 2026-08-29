/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class DocumentConstant {

    private DocumentConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * mapper扫描包名.
     */
    public static final String DOCUMENT_MAPPER_SCAN_BASE_PACKAGE = "com.iwindplus.base.document.dal.mapper";

    /**
     * bean扫描包名.
     */
    public static final String DOCUMENT_COMPONENT_SCAN_BASE_PACKAGE = "com.iwindplus.base.document";

    /**
     * 导出任务相关常数 .
     */
    public final class ExportTaskConstant {
        /**
         * 钩子方法名：主任务执行成功.
         */
        public static final String HOOK_ON_TASK_SUCCESS = "onTaskSuccess";

        /**
         * 钩子方法名：主任务执行失败.
         */
        public static final String HOOK_ON_TASK_FAIL = "onTaskFail";
    }
}
