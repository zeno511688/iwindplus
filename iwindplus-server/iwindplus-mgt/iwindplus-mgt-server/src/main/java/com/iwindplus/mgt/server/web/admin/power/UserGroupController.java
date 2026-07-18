/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.web.admin.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.redis.domain.annotation.RedisRepeatSubmit;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.power.UserGroupSaveEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserGroupSearchDTO;
import com.iwindplus.mgt.domain.vo.power.UserGroupBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupPageVO;
import com.iwindplus.mgt.server.service.power.UserGroupService;
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
 * 用户组相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "用户组接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/userGroup")
@Validated
@RequiredArgsConstructor
public class UserGroupController extends BaseController {

    private final UserGroupService userGroupService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "userGroup", operateType = "save", operateName = "添加", operateDesc = "添加用户组")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) UserGroupSaveEditDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        boolean data = this.userGroupService.save(entity);
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
    @OperateLog(bizType = "userGroup", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除用户组")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.userGroupService.removeByIds(ids);
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
    @RedisRepeatSubmit
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "userGroup", operateType = "edit", operateName = "编辑", operateDesc = "编辑用户组")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) UserGroupSaveEditDTO entity) {
        boolean data = this.userGroupService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "userGroup", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑用户组状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.userGroupService.editStatus(id, status);
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
    @OperateLog(keys = {"#id"}, bizType = "userGroup", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑用户组设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.userGroupService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < UserGroupPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<UserGroupPageVO>> page(@Validated UserGroupSearchDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<UserGroupPageVO> data = this.userGroupService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 用户所属用户组.
     *
     * @param userId 用户主键
     * @return ResultVO < List < UserGroupBaseCheckedVO>>
     */
    @Operation(summary = "用户所属用户组")
    @GetMapping("listByUserId")
    public ResultVO<List<UserGroupBaseCheckedVO>> listByUserId(@RequestParam(required = false) Long userId) {
        Long orgId = this.getUserInfo().getOrgId();
        List<UserGroupBaseCheckedVO> data = this.userGroupService.listByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    /**
     * 角色所属用户组.
     *
     * @param roleId 角色主键
     * @return ResultVO < List < UserGroupBaseCheckedVO>>
     */
    @Operation(summary = "角色所属用户组")
    @GetMapping("listByRoleId")
    public ResultVO<List<UserGroupBaseCheckedVO>> listByRoleId(@RequestParam(required = false) Long roleId) {
        Long orgId = this.getUserInfo().getOrgId();
        List<UserGroupBaseCheckedVO> data = this.userGroupService.listByRoleId(orgId, roleId);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO < UserGroupExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<UserGroupExtendVO> getDetailExtend(@RequestParam Long id) {
        UserGroupExtendVO data = this.userGroupService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
