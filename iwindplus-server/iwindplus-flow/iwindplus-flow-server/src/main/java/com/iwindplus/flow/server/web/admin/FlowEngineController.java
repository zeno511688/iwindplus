/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.web.admin;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.flow.domain.dto.FlowAddTaskPlayerDTO;
import com.iwindplus.flow.domain.dto.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.dto.FlowDelegateTaskDTO;
import com.iwindplus.flow.domain.dto.FlowJumpTaskDTO;
import com.iwindplus.flow.domain.dto.FlowRejectTaskDTO;
import com.iwindplus.flow.domain.dto.FlowRemoveTaskPlayerDTO;
import com.iwindplus.flow.domain.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.domain.dto.FlowTransferTaskDTO;
import com.iwindplus.flow.domain.vo.FlowStartInstanceVO;
import com.iwindplus.flow.server.core.FlowEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程引擎操作接口定义类.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
@Tag(name = "流程引擎接口")
@Slf4j
@RestController
@RequestMapping("admin/flow/engine")
@Validated
@RequiredArgsConstructor
public class FlowEngineController extends BaseController {

    private final FlowEngine flowEngine;

    /**
     * 发起流程.
     *
     * @param entity 对象
     * @return ResultVO<FlowStartInstanceVO>
     */
    @Operation(summary = "发起流程")
    @PostMapping("startInstance")
    public ResultVO<FlowStartInstanceVO> startInstance(@Validated @RequestBody FlowStartInstanceDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.instanceAction().startInstance(entity));
    }

    /**
     * 撤销流程.
     *
     * @param instanceId 实例主键
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "撤销流程")
    @PostMapping("revokeInstance")
    public ResultVO<Boolean> revokeInstance(@RequestParam Long instanceId) {
        return ResultVO.success(flowEngine.instanceAction().revokeInstance(instanceId, this.getUserInfo()));
    }

    /**
     * 终止流程.
     *
     * @param instanceId 实例主键
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "终止流程")
    @PostMapping("terminateInstance")
    public ResultVO<Boolean> terminateInstance(@RequestParam Long instanceId) {
        return ResultVO.success(flowEngine.instanceAction().terminateInstance(instanceId, this.getUserInfo()));
    }

    /**
     * 审批通过.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "审批通过")
    @PostMapping("approveTask")
    public ResultVO<Boolean> approveTask(@Validated @RequestBody FlowApproveTaskDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().approveTask(entity));
    }

    /**
     * 驳回.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "驳回")
    @PostMapping("rejectTask")
    public ResultVO<Boolean> rejectTask(@Validated @RequestBody FlowRejectTaskDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().rejectTask(entity));
    }

    /**
     * 转交任务.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "转交任务")
    @PostMapping("transferTask")
    public ResultVO<Boolean> transferTask(@Validated @RequestBody FlowTransferTaskDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().transferTask(entity));
    }

    /**
     * 加签.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "加签")
    @PostMapping("addTaskPlayer")
    public ResultVO<Boolean> addTaskPlayer(@Validated @RequestBody FlowAddTaskPlayerDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().addTaskPlayer(entity));
    }

    /**
     * 减签.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "减签")
    @PostMapping("removeTaskPlayer")
    public ResultVO<Boolean> removeTaskPlayer(@Validated @RequestBody FlowRemoveTaskPlayerDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().removeTaskPlayer(entity));
    }

    /**
     * 流程跳转.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "流程跳转")
    @PostMapping("jumpTask")
    public ResultVO<Boolean> jumpTask(@Validated @RequestBody FlowJumpTaskDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().jumpTask(entity));
    }

    /**
     * 委托任务.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "委托任务")
    @PostMapping("delegateTask")
    public ResultVO<Boolean> delegateTask(@Validated @RequestBody FlowDelegateTaskDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        return ResultVO.success(flowEngine.taskAction().delegateTask(entity));
    }
}