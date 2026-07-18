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
import com.iwindplus.flow.domain.dto.FlowCategoryEditDTO;
import com.iwindplus.flow.domain.dto.FlowCategorySaveDTO;
import com.iwindplus.flow.domain.dto.FlowCategorySearchDTO;
import com.iwindplus.flow.domain.vo.FlowCategoryBaseVO;
import com.iwindplus.flow.domain.vo.FlowCategoryPageVO;
import com.iwindplus.flow.domain.vo.FlowCategoryVO;
import com.iwindplus.flow.server.service.FlowCategoryService;
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
 * 流程分类相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "流程分类接口")
@Slf4j
@RestController
@RequestMapping("admin/flow/category")
@Validated
@RequiredArgsConstructor
public class FlowCategoryController extends BaseController {

    private final FlowCategoryService flowCategoryService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "flowCategory", operateType = "save", operateName = "添加", operateDesc = "添加流程分类")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) FlowCategorySaveDTO entity) {
        boolean data = this.flowCategoryService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "flowCategory", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除流程分类")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.flowCategoryService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "flowCategory", operateType = "edit", operateName = "编辑", operateDesc = "编辑流程分类")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) FlowCategoryEditDTO entity) {
        boolean data = this.flowCategoryService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "flowCategory", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑流程分类状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.flowCategoryService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "flowCategory", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑流程分类设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.flowCategoryService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < FlowCategoryPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<FlowCategoryPageVO>> page(@Validated FlowCategorySearchDTO entity) {
        IPage<FlowCategoryPageVO> data = this.flowCategoryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < FlowCategoryBaseVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<FlowCategoryBaseVO>> listEnabled() {
        List<FlowCategoryBaseVO> data = this.flowCategoryService.listEnabled();
        return ResultVO.success(data);
    }

    /**
     * 查看详情.
     *
     * @param id 主键
     * @return ResultVO < FlowCategoryVO>
     */
    @Operation(summary = "查看详情")
    @GetMapping("getDetail")
    public ResultVO<FlowCategoryVO> getDetail(@RequestParam Long id) {
        FlowCategoryVO data = this.flowCategoryService.getDetail(id);
        return ResultVO.success(data);
    }
}
