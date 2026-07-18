/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.flow.domain.dto.FlowFormEditDTO;
import com.iwindplus.flow.domain.dto.FlowFormSaveDTO;
import com.iwindplus.flow.domain.dto.FlowFormSearchDTO;
import com.iwindplus.flow.domain.vo.FlowFormBaseExtendVO;
import com.iwindplus.flow.domain.vo.FlowFormPageVO;
import com.iwindplus.flow.domain.vo.FlowFormVO;
import com.iwindplus.flow.server.service.FlowFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程表单相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "流程表单接口")
@Slf4j
@RestController
@RequestMapping("admin/flow/form")
@Validated
@RequiredArgsConstructor
public class FlowFormController extends BaseController {

    private final FlowFormService flowFormService;

    /**
     * 保存，id非必填.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加流程表单")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "flowForm", operateType = "save", operateName = "添加", operateDesc = "添加流程表单")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) FlowFormSaveDTO entity) {
        boolean data = this.flowFormService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "flowForm", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除流程表单")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.flowFormService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑，id必填.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑流程表单")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "flowForm", operateType = "edit", operateName = "编辑", operateDesc = "编辑流程表单")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) FlowFormEditDTO entity) {
        boolean data = this.flowFormService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑状态")
    @PutMapping("editStatus")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "flowForm", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑流程分类状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.flowFormService.editStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑设为内置")
    @PutMapping("editBuildIn")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "flowForm", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑流程表单设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.flowFormService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < FlowFormPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<FlowFormPageVO>> page(@Validated FlowFormSearchDTO entity) {
        IPage<FlowFormPageVO> data = this.flowFormService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < FlowFormBaseExtendVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<FlowFormBaseExtendVO>> listEnabled() {
        List<FlowFormBaseExtendVO> data = this.flowFormService.listEnabled();
        return ResultVO.success(data);
    }

    /**
     * 查看详情.
     *
     * @param id 主键
     * @return ResultVO < FlowFormVO>
     */
    @Operation(summary = "查看详情")
    @GetMapping("getDetail")
    public ResultVO<FlowFormVO> getDetail(@RequestParam Long id) {
        FlowFormVO data = this.flowFormService.getDetail(id);
        return ResultVO.success(data);
    }
}
