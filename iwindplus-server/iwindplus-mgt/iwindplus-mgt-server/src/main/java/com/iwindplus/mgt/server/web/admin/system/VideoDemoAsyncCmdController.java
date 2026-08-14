/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.system;

import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubmitVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.server.service.asynccmd.video.VideoDemoAsyncCmdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异步命令视频合成演示接口（测试用例调用入口）.
 *
 * <p>验证路径：
 * 1、POST submitMergeGroup 提交组任务，记录返回的bizNumber与日志中的submitBizNumber
 * 2、观察日志：素材准备与提交第三方并发执行，提交第三方转ASYNC_WAIT主任务转回待执行
 * 3、POST callbackSuccess 模拟第三方回调，观察占位进度逐个点亮、结果转存、主收尾成功
 * （或 POST callbackFailed 观察子任务失败进重试链）</p>
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Tag(name = "异步命令视频合成演示接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/videoDemo")
@Validated
@RequiredArgsConstructor
public class VideoDemoAsyncCmdController extends BaseController {

    private final VideoDemoAsyncCmdService videoDemoAsyncCmdService;

    /**
     * 提交组任务（本地任务+第三方回调+进度占位+后续任务）.
     *
     * @param videoId 视频业务ID
     * @return ResultVO<AsyncCmdSubmitVO>
     */
    @Operation(summary = "提交视频合成组任务")
    @PostMapping("submitMergeGroup")
    public ResultVO<AsyncCmdSubmitVO> submitMergeGroup(@RequestParam String videoId) {
        AsyncCmdSubmitVO data = this.videoDemoAsyncCmdService.submitMergeGroup(videoId);
        return ResultVO.success(data);
    }

    /**
     * 提交单任务.
     *
     * @param videoId 视频业务ID
     * @return ResultVO<AsyncCmdSubmitVO>
     */
    @Operation(summary = "提交视频合成单任务")
    @PostMapping("submitSingle")
    public ResultVO<AsyncCmdSubmitVO> submitSingle(@RequestParam String videoId) {
        AsyncCmdSubmitVO data = this.videoDemoAsyncCmdService.submitSingle(videoId);
        return ResultVO.success(data);
    }

    /**
     * 模拟第三方回调成功.
     *
     * @param submitBizNumber 提交第三方子任务的bizNumber（提交时日志打印）
     * @param fileUrl         第三方返回的合成文件地址
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "模拟第三方回调成功")
    @PostMapping("callbackSuccess")
    public ResultVO<Boolean> callbackSuccess(
        @RequestParam String submitBizNumber, @RequestParam String fileUrl) {
        this.videoDemoAsyncCmdService.mockThirdCallbackSuccess(submitBizNumber, fileUrl);
        return ResultVO.success(Boolean.TRUE);
    }

    /**
     * 模拟第三方回调失败.
     *
     * @param submitBizNumber 提交第三方子任务的bizNumber（提交时日志打印）
     * @param errorMsg        错误信息
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "模拟第三方回调失败")
    @PostMapping("callbackFailed")
    public ResultVO<Boolean> callbackFailed(
        @RequestParam String submitBizNumber, @RequestParam String errorMsg) {
        this.videoDemoAsyncCmdService.mockThirdCallbackFailed(submitBizNumber, errorMsg);
        return ResultVO.success(Boolean.TRUE);
    }

    /**
     * 丢弃任务手动重试.
     *
     * @param bizNumber 主任务业务流水号
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "丢弃任务手动重试")
    @PostMapping("retry")
    public ResultVO<Boolean> retry(@RequestParam String bizNumber) {
        this.videoDemoAsyncCmdService.retryByBizNumber(bizNumber);
        return ResultVO.success(Boolean.TRUE);
    }
}
