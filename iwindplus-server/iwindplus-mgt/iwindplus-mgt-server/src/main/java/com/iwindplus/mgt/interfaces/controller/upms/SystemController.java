/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.upms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.query.system.app.SystemQueryService;
import com.iwindplus.mgt.application.query.system.app.vo.SystemBaseVO;
import com.iwindplus.mgt.application.query.system.app.vo.SystemExtendVO;
import com.iwindplus.mgt.application.query.system.app.vo.SystemPageVO;
import com.iwindplus.mgt.application.service.system.app.SystemApplicationService;
import com.iwindplus.mgt.application.service.system.app.dto.SystemEditDTO;
import com.iwindplus.mgt.application.service.system.app.dto.SystemSaveDTO;
import com.iwindplus.mgt.application.service.system.app.dto.SystemSearchDTO;
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
 * 系统相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "系统接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/system")
@Validated
@RequiredArgsConstructor
public class SystemController extends BaseController {

    private final SystemApplicationService systemApplicationService;
    private final SystemQueryService systemQueryService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "system", operateType = "save", operateName = "添加", operateDesc = "添加系统")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) SystemSaveDTO entity) {
        boolean data = this.systemApplicationService.save(entity);
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
    @OperateLog(bizType = "system", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除系统")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.systemApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑系统")
    @PutMapping("edit")
    @OperateLog(keys = "#entity.id", bizType = "system", operateType = "edit", operateName = "编辑", operateDesc = "编辑系统")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) SystemEditDTO entity) {
        boolean data = this.systemApplicationService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "system", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑系统状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.systemApplicationService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "system", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑系统设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.systemApplicationService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < SystemPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<SystemPageVO>> page(@Validated SystemSearchDTO entity) {
        IPage<SystemPageVO> data = this.systemQueryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO < List < SystemBaseVO>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<SystemBaseVO>> listEnabled() {
        List<SystemBaseVO> data = this.systemQueryService.listByEnabled();
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO<SystemExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<SystemExtendVO> getDetailExtend(@RequestParam Long id) {
        SystemExtendVO data = this.systemQueryService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
