/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.upms;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.query.upms.organization.DepartmentQueryService;
import com.iwindplus.mgt.application.query.upms.organization.vo.DepartmentExtendVO;
import com.iwindplus.mgt.application.service.upms.organization.DepartmentApplicationService;
import com.iwindplus.mgt.application.service.upms.organization.dto.DepartmentEditDTO;
import com.iwindplus.mgt.application.service.upms.organization.dto.DepartmentSaveDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
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
 * 部门相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "部门接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/department")
@Validated
@RequiredArgsConstructor
public class DepartmentController extends BaseController {

    private final DepartmentApplicationService departmentApplicationService;
    private final DepartmentQueryService departmentQueryService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "department", operateType = "save", operateName = "添加", operateDesc = "添加部门")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) DepartmentSaveDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.departmentApplicationService.save(entity);
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
    @OperateLog(bizType = "department", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除部门")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.departmentApplicationService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "department", operateType = "edit", operateName = "编辑", operateDesc = "编辑部门")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) DepartmentEditDTO entity) {
        boolean data = this.departmentApplicationService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "department", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑部门状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.departmentApplicationService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "department", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑部门设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.departmentApplicationService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 组织部门列表.
     *
     * @return ResultVO < List < Tree < Long>>>
     */
    @Operation(summary = "组织部门列表")
    @GetMapping("listByOrgId")
    public ResultVO<List<Tree<Long>>> listByOrgId(@RequestParam(required = false) Long orgId) {
        orgId = Optional.ofNullable(orgId).orElse(this.getUserInfo().getOrgId());
        List<Tree<Long>> data = this.departmentQueryService.listByOrgId(orgId);
        return ResultVO.success(data);
    }

    /**
     * 启用的组织部门.
     *
     * @return ResultVO < List < Tree < Long>>>
     */
    @Operation(summary = "启用的组织部门")
    @GetMapping("listEnabledByOrgId")
    public ResultVO<List<Tree<Long>>> listEnabledByOrgId(@RequestParam(required = false) Long orgId) {
        orgId = Optional.ofNullable(orgId).orElse(this.getUserInfo().getOrgId());
        List<Tree<Long>> data = this.departmentQueryService.listEnabledByOrgId(orgId);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO<DepartmentExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<DepartmentExtendVO> getDetailExtend(@RequestParam Long id) {
        DepartmentExtendVO data = this.departmentQueryService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
