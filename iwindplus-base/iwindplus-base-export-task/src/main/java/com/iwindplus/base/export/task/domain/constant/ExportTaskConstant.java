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

    /**
     * 导出批次大小（每页查询条数）.
     */
    public static final Long EXPORT_BATCH_SIZE = 1000L;

    /**
     * 首页页码.
     */
    public static final Long FIRST_PAGE_INDEX = 1L;

    /**
     * 第二页页码.
     */
    public static final Long SECOND_PAGE_INDEX = 2L;

    /**
     * 进度百分比基数.
     */
    public static final double PROGRESS_PERCENT_BASE = 100.0;

    /**
     * OSS相对路径默认前缀.
     */
    public static final String OSS_RELATIVE_PATH_PREFIX = "export-task/";

    /**
     * 路径分隔符.
     */
    public static final String PATH_SEPARATOR = "/";
}
