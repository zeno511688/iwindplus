/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.domain.dto.RegionEditDTO;
import com.iwindplus.setup.domain.dto.RegionSaveDTO;
import com.iwindplus.setup.domain.vo.RegionVO;
import com.iwindplus.setup.server.service.RegionService;
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
 * 省市区相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "省市区接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/region")
@Validated
@RequiredArgsConstructor
public class RegionController extends BaseController {

    private final RegionService regionService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "region", operateType = "save", operateName = "添加", operateDesc = "添加省市区")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) RegionSaveDTO entity) {
        boolean data = this.regionService.save(entity);
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
    @OperateLog(bizType = "region", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除省市区")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.regionService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑省市区")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "region", operateType = "edit", operateName = "编辑", operateDesc = "编辑省市区")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) RegionEditDTO entity) {
        boolean data = this.regionService.edit(entity);
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
    @OperateLog(keys = "#entity.id", bizType = "region", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑省市区状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.regionService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "region", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑省市区设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.regionService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 启用的列表.
     *
     * @return ResultVO<List < Tree < Long>>>
     */
    @Operation(summary = "启用的列表")
    @GetMapping("listEnabled")
    public ResultVO<List<Tree<Long>>> listEnabled() {
        List<Tree<Long>> data = this.regionService.listByEnabled(EnableStatusEnum.ENABLE);
        return ResultVO.success(data);
    }

    /**
     * 所有列表.
     *
     * @return ResultVO < List < Tree < Long>>>>
     */
    @Operation(summary = "所有列表")
    @GetMapping("listAll")
    public ResultVO<List<Tree<Long>>> listAll() {
        List<Tree<Long>> data = this.regionService.listByEnabled(null);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < RegionVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<RegionVO> getDetail(@RequestParam Long id) {
        RegionVO data = this.regionService.getDetail(id);
        return ResultVO.success(data);
    }
}
