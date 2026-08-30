/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.support;

import com.iwindplus.base.async.task.domain.vo.AsyncTaskCallbackResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskExecuteResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;

/**
 * 异步任务子任务处理器.
 *
 * <p>有子任务时，组任务的业务逻辑全部落在这里，子任务按seq升序执行，每个子任务回调自己的实现，已成功的任务会被跳过</p>
 * <p>每个子任务成功或失败执行onSubTaskSuccess/onSubTaskFail</p>
 *
 * @author zengdegui
 * @since 2025/09/21 20:18
 */
public interface AsyncTaskSubHandler {

    /**
     * 获取执行器名称（有默认值不需要实现）.
     *
     * @return 执行器名称
     */
    default String getExecuteName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 执行子业务.
     *
     * <p>由业务方显式返回执行结果，决定子任务状态走向：
     * SUCCESS → 成功，FAILED → 失败，ASYNC_WAIT → 进入异步等待</p>
     *
     * @param entity 对象
     * @return AsyncTaskExecuteResultVO
     */
    AsyncTaskExecuteResultVO executeSub(AsyncTaskSubVO entity);

    /**
     * 子任务获取异步等待结果.
     *
     * @param entity 子任务对象
     * @return AsyncTaskCallbackResultVO
     */
    default AsyncTaskCallbackResultVO executeSubCallback(AsyncTaskSubVO entity) {
        return AsyncTaskCallbackResultVO.waiting();
    }

    /**
     * 子任务成功.
     *
     * @param entity 子任务对象
     */
    default void onSubTaskSuccess(AsyncTaskSubVO entity) {
    }

    /**
     * 子任务失败.
     *
     * @param entity 子任务对象
     */
    default void onSubTaskFail(AsyncTaskSubVO entity) {
    }

    /**
     * 子任务异步等待，首次挂起时触发.
     *
     * @param entity 子任务对象
     */
    default void onSubTaskAsyncWait(AsyncTaskSubVO entity) {

    }
}
