/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynctask.video;

import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskCallbackDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskExtDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskGroupSubmitDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubCallbackDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubExtDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubSubmitDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubmitDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskCallbackResultEnum;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubmitVO;
import com.iwindplus.base.async.task.executor.AsyncTaskExecutor;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频合成示例：异步任务演示服务.
 *
 * <p>覆盖README中的全部调度方式示例：
 * 组任务（本地任务+第三方回调+进度占位+后续任务）、单任务、回调上报、重试</p>
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoDemoAsyncTaskService {

    private final AsyncTaskExecutor asyncTaskExecutor;

    /**
     * 用例1：组任务提交.
     *
     * <p>编排：素材准备(seq=1) 与 提交第三方(seq=2) 同stage=1并发 →
     * 第三方回调成功后，3个进度占位(seq=3~5)按序点亮 → 结果转存(seq=6) → 主收尾</p>
     *
     * @param videoId 视频业务ID
     * @return AsyncTaskSubmitVO 提交结果（含主任务bizNumber）
     */
    public AsyncTaskSubmitVO submitMergeGroup(String videoId) {
        // 第三方回调定位键：须全局唯一，作为第三方请求流水号传给第三方
        final String submitBizNumber = "VID-" + IdUtil.fastSimpleUUID();

        final AsyncTaskSubmitVO result = asyncTaskExecutor.submitGroup(AsyncTaskGroupSubmitDTO.builder()
            .bizName("视频合成").bizKey("VIDEO").bizType("VIDEO_MERGE")
            .executorClass(VideoDemoMergeTaskHandler.class)
            .needDisplay(true)
            .param(Map.of("videoId", videoId))
            .ext(AsyncTaskExtDTO.builder().maxAttempts(10).extra(Map.of("videoId", videoId)).build())
            .subTasks(List.of(
                // seq=1 本地任务（与seq=2同stage=1，批内并发）
                AsyncTaskSubSubmitDTO.builder()
                    .bizName("素材准备").bizKey("VIDEO").bizType("VIDEO_PREPARE")
                    .seq(1).stage(1)
                    .executorClass(VideoDemoPrepareSubHandler.class)
                    .param(Map.of("videoId", videoId))
                    .ext(AsyncTaskSubExtDTO.builder().extra(Map.of("videoId", videoId)).build())
                    .build(),
                // seq=2 调第三方，提交后等回调
                AsyncTaskSubSubmitDTO.builder()
                    .bizName("提交第三方合成").bizKey("VIDEO").bizType("VIDEO_SUBMIT")
                    .bizNumber(submitBizNumber)
                    .seq(2).stage(1)
                    .executorClass(VideoDemoSubmitSubHandler.class)
                    .param(Map.of("videoId", videoId))
                    .needCallback(true)
                    .build(),
                AsyncTaskSubSubmitDTO.builder()
                    .bizName("合成中").bizKey("VIDEO").bizType("VIDEO_PROGRESS")
                    .seq(3).stage(1)
                    .executorClass(VideoDemoSynthesisSubHandler.class)
                    .needDisplay(true)
                    .build(),
                AsyncTaskSubSubmitDTO.builder()
                    .bizName("转码中").bizKey("VIDEO").bizType("VIDEO_PROGRESS")
                    .seq(4).stage(1)
                    .executorClass(VideoDemoTransSubHandler.class)
                    .needDisplay(true)
                    .build(),
                AsyncTaskSubSubmitDTO.builder()
                    .bizName("上传中").bizKey("VIDEO").bizType("VIDEO_PROGRESS")
                    .seq(5).stage(1)
                    .executorClass(VideoDemoUploadSubHandler.class)
                    .needDisplay(true)
                    .build(),
                // seq=6 后续任务，读取前置批次结果
                AsyncTaskSubSubmitDTO.builder()
                    .bizName("结果转存").bizKey("VIDEO").bizType("VIDEO_SAVE")
                    .seq(6).stage(2)
                    .executorClass(VideoDemoSaveSubHandler.class)
                    .param(Map.of("videoId", videoId))
                    .build()
            )).build());

        log.info("videoDemo submitMergeGroup success, id={} bizNumber={} submitBizNumber={}",
            result.getId(), result.getBizNumber(), submitBizNumber);
        return result;
    }

    /**
     * 用例2：单任务提交.
     *
     * @param videoId 视频业务ID
     * @return AsyncTaskSubmitVO 提交结果
     */
    public AsyncTaskSubmitVO submitSingle(String videoId) {
        return asyncTaskExecutor.submit(AsyncTaskSubmitDTO.builder()
            .bizName("视频合成").bizKey("VIDEO").bizType("VIDEO_MERGE")
            .bizNumber("VID-" + IdUtil.fastSimpleUUID())
            .param(Map.of("videoId", videoId))
            .executorClass(VideoDemoMergeTaskHandler.class)
            .needDisplay(true)
            .build());
    }

    /**
     * 用例3：模拟第三方回调成功（业务回调接口收到第三方通知后调用）.
     *
     * @param submitBizNumber 提交第三方时使用的bizNumber
     * @param fileUrl         第三方返回的合成文件地址
     */
    public void mockThirdCallbackSuccess(String submitBizNumber, String fileUrl) {
        asyncTaskExecutor.callback(AsyncTaskCallbackDTO.builder()
            .subTasks(List.of(
                AsyncTaskSubCallbackDTO.builder()
                    .bizNumber(submitBizNumber)
                    .callbackResult(AsyncTaskCallbackResultEnum.SUCCESS)
                    .result(Map.of("fileUrl", fileUrl))
                    .build()
            ))
            .build());
    }

    /**
     * 用例4：模拟第三方回调失败.
     *
     * @param submitBizNumber 提交第三方时使用的bizNumber
     * @param errorMsg        错误信息
     */
    public void mockThirdCallbackFailed(String submitBizNumber, String errorMsg) {
        asyncTaskExecutor.callback(AsyncTaskCallbackDTO.builder()
            .subTasks(List.of(
                AsyncTaskSubCallbackDTO.builder()
                    .bizNumber(submitBizNumber)
                    .callbackResult(AsyncTaskCallbackResultEnum.FAILED)
                    .errorMsg(errorMsg)
                    .build()
            ))
            .build());
    }

    /**
     * 用例5：丢弃任务手动重试.
     *
     * @param bizNumber 主任务业务流水号
     */
    public void retryByBizNumber(String bizNumber) {
        asyncTaskExecutor.retryByBizNumber(bizNumber);
    }
}
