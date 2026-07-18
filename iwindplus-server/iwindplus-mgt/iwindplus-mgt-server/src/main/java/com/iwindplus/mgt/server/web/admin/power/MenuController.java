/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.power;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.power.MenuEditDTO;
import com.iwindplus.mgt.domain.dto.power.MenuSaveDTO;
import com.iwindplus.mgt.domain.vo.power.MenuExtendVO;
import com.iwindplus.mgt.domain.vo.power.MenuTreeSystemVO;
import com.iwindplus.mgt.server.service.power.MenuService;
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
 * 菜单相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "菜单接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/menu")
@Validated
@RequiredArgsConstructor
public class MenuController extends BaseController {

    private final MenuService menuService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "menu", operateType = "save", operateName = "添加", operateDesc = "添加菜单")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) MenuSaveDTO entity) {
        boolean data = this.menuService.save(entity);
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
    @OperateLog(bizType = "menu", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除菜单")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.menuService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "menu", operateType = "edit", operateName = "编辑", operateDesc = "编辑菜单")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) MenuEditDTO entity) {
        boolean data = this.menuService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "menu", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑菜单状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.menuService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "menu", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑菜单设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.menuService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 系统菜单.
     *
     * @param systemId 系统主键
     * @return ResultVO < List < Tree < Long>>>
     */
    @Operation(summary = "系统菜单")
    @GetMapping("listBySystemId")
    public ResultVO<List<Tree<Long>>> listBySystemId(@RequestParam Long systemId) {
        List<Tree<Long>> data = this.menuService.listBySystemId(systemId);
        return ResultVO.success(data);
    }

    /**
     * 启用的系统菜单.
     *
     * @param systemId 系统主键
     * @return ResultVO < List < Tree < Long>>>
     */
    @Operation(summary = "启用的系统菜单")
    @GetMapping("listEnabledBySystemId")
    public ResultVO<List<Tree<Long>>> listEnabledBySystemId(@RequestParam Long systemId) {
        List<Tree<Long>> data = this.menuService.listEnabledBySystemId(systemId);
        return ResultVO.success(data);
    }

    /**
     * 用户菜单权限.
     *
     * @return ResultVO < List < MenuTreeSystemVO>>
     */
    @Operation(summary = "用户菜单权限")
    @GetMapping("listByUserId")
    public ResultVO<List<MenuTreeSystemVO>> listByUserId() {
        final Long orgId = this.getUserInfo().getOrgId();
        final Long userId = this.getUserInfo().getUserId();
        List<MenuTreeSystemVO> data = this.menuService.listByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    /**
     * 角色所属菜单.
     *
     * @param roleId 角色主键
     * @return ResultVO < List < MenuTreeSystemVO>>
     */
    @Operation(summary = "角色所属菜单")
    @GetMapping("listByRoleId")
    public ResultVO<List<MenuTreeSystemVO>> listByRoleId(@RequestParam(required = false) Long roleId) {
        final Long orgId = this.getUserInfo().getOrgId();
        List<MenuTreeSystemVO> data = this.menuService.listByRoleId(orgId, roleId);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO<MenuExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<MenuExtendVO> getDetailExtend(@RequestParam Long id) {
        MenuExtendVO data = this.menuService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
