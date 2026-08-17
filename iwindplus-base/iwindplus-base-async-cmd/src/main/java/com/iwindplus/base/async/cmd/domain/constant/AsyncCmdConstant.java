/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class AsyncCmdConstant {

    private AsyncCmdConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 异步命令mapper扫描包名.
     */
    public static final String ASYNC_CMD_MAPPER_SCAN_BASE_PACKAGE = "com.iwindplus.base.async.cmd.dal.mapper";

    /**
     * 异步命令bean扫描包名.
     */
    public static final String ASYNC_CMD_COMPONENT_SCAN_BASE_PACKAGE = "com.iwindplus.base.async.cmd";

    /**
     * 预存回调结果在result中的保留键.
     */
    public static final String CALLBACK_RESULT_KEY = "callbackResult";

    /**
     * 预存回调错误信息在result中的保留键.
     */
    public static final String CALLBACK_ERROR_MSG_KEY = "callbackErrorMsg";

    /**
     * 反射方法名：主任务回调执行方法.
     */
    public static final String METHOD_EXECUTE_CALLBACK = "executeCallback";

    /**
     * 反射方法名：子任务回调执行方法.
     */
    public static final String METHOD_EXECUTE_SUB_CALLBACK = "executeSubCallback";

    /**
     * 钩子方法名：主任务执行成功.
     */
    public static final String HOOK_ON_TASK_SUCCESS = "onTaskSuccess";

    /**
     * 钩子方法名：主任务执行失败.
     */
    public static final String HOOK_ON_TASK_FAIL = "onTaskFail";

    /**
     * 钩子方法名：主任务异步等待.
     */
    public static final String HOOK_ON_TASK_ASYNC_WAIT = "onTaskAsyncWait";

    /**
     * 钩子方法名：子任务执行成功.
     */
    public static final String HOOK_ON_SUB_TASK_SUCCESS = "onSubTaskSuccess";

    /**
     * 钩子方法名：子任务执行失败.
     */
    public static final String HOOK_ON_SUB_TASK_FAIL = "onSubTaskFail";

    /**
     * 钩子方法名：子任务异步等待.
     */
    public static final String HOOK_ON_SUB_TASK_ASYNC_WAIT = "onSubTaskAsyncWait";
}
