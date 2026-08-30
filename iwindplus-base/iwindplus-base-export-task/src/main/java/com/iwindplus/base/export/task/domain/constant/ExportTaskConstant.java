/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class ExportTaskConstant {

    private ExportTaskConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * mapper扫描包名.
     */
    public static final String EXPORT_MAPPER_SCAN_BASE_PACKAGE = "com.iwindplus.base.export.task.dal.mapper";

    /**
     * bean扫描包名.
     */
    public static final String EXPORT_COMPONENT_SCAN_BASE_PACKAGE = "com.iwindplus.base.export.task";

    /**
     * 线程池bean名称.
     */
    public static final String THREAD_POOL_BEAN_NAME = "exportTaskThreadPool";

    /**
     * 钩子方法名：主任务执行成功.
     */
    public static final String HOOK_ON_TASK_SUCCESS = "onTaskSuccess";

    /**
     * 钩子方法名：主任务执行失败.
     */
    public static final String HOOK_ON_TASK_FAIL = "onTaskFail";
}
