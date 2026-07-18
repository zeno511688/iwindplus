/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.web.admin.power;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.power.RoleEditDTO;
import com.iwindplus.mgt.domain.dto.power.RoleGrantMenuDTO;
import com.iwindplus.mgt.domain.dto.power.RoleGrantResourceDTO;
import com.iwindplus.mgt.domain.dto.power.RoleGrantUserGroupDTO;
import com.iwindplus.mgt.domain.dto.power.RoleSaveDTO;
import com.iwindplus.mgt.domain.dto.power.RoleSearchDTO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.domain.vo.power.RoleExtendVO;
import com.iwindplus.mgt.domain.vo.power.RolePageVO;
import com.iwindplus.mgt.domain.vo.power.UserOrgInfoVO;
import com.iwindplus.mgt.server.service.WsPushService;
import com.iwindplus.mgt.server.service.power.RoleMenuService;
import com.iwindplus.mgt.server.service.power.RoleResourceService;
import com.iwindplus.mgt.server.service.power.RoleService;
import com.iwindplus.mgt.server.service.power.UserGroupRoleService;
import com.iwindplus.mgt.server.service.power.UserService;
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
 * 角色相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "角色接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/role")
@Validated
@RequiredArgsConstructor
public class RoleController extends BaseController {

    private final RoleService roleService;
    private final RoleMenuService roleMenuService;
    private final RoleResourceService roleResourceService;
    private final UserGroupRoleService groupRoleService;
    private final UserService userService;
    private final WsPushService wsPushService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "role", operateType = "save", operateName = "添加", operateDesc = "添加角色")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) RoleSaveDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.roleService.save(entity);
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
    @OperateValid
    @OperateLog(bizType = "role", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除菜单")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.roleService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑角色")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "role", operateType = "edit", operateName = "编辑", operateDesc = "编辑角色")
    @PutMapping("edit")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) RoleEditDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.roleService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "role", operateType = "editStatus", operateName = "编辑状态", operateDesc = "编辑角色状态")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam EnableStatusEnum status) {
        boolean data = this.roleService.editStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 编辑设为默认.
     *
     * @param id 主键
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑设为默认")
    @PutMapping("editDefault")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "role", operateType = "editDefault", operateName = "编辑设为默认", operateDesc = "编辑角色设为默认")
    public ResultVO<Boolean> editDefault(@RequestParam Long id) {
        Long orgId = this.getUserInfo().getOrgId();
        boolean data = this.roleService.editDefault(id, orgId);
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
    @OperateLog(keys = {"#id"}, bizType = "role", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑角色设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.roleService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 授权菜单.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "授权菜单")
    @PutMapping("editBatchMenu")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.roleId"}, bizType = "role", operateType = "editBatchMenu", operateName = "授权菜单", operateDesc = "角色授权菜单")
    public ResultVO<Boolean> editBatchMenu(@RequestBody @Validated RoleGrantMenuDTO entity) {
        boolean data = this.roleMenuService.editBatchMenu(entity.getRoleId(), entity.getMenuIds());
        return ResultVO.success(data);
    }

    /**
     * 授权资源.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "授权资源")
    @PutMapping("editBatchResource")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.roleId"}, bizType = "role", operateType = "editBatchResource", operateName = "授权资源", operateDesc = "角色授权资源")
    public ResultVO<Boolean> editBatchResource(@RequestBody @Validated RoleGrantResourceDTO entity) {
        boolean data = this.roleResourceService.editBatchResource(entity.getRoleId(), entity.getResourceIds());

        final UserBaseVO userInfo = this.getUserInfo();
        final List<UserOrgInfoVO> userOrgInfoList = this.userService.listByRoleIds(List.of(entity.getRoleId()));
        if (CollUtil.isNotEmpty(userOrgInfoList)) {
            userOrgInfoList.parallelStream().forEach(userOrgInfo ->
                this.wsPushService.sendWsButtonPermission(userOrgInfo.getUserId(), userOrgInfo.getOrgId()
                    , userInfo.getOrgId(), userInfo.getUserId()));
        }

        return ResultVO.success(data);
    }

    /**
     * 授权用户组.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "授权用户组")
    @PutMapping("editBatchUserGroup")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.roleId"}, bizType = "role", operateType = "editBatchUserGroup", operateName = "授权用户组", operateDesc = "角色授权用户组")
    public ResultVO<Boolean> editBatchUserGroup(@RequestBody @Validated RoleGrantUserGroupDTO entity) {
        boolean data = this.groupRoleService.editBatchUserGroup(entity.getRoleId(), entity.getUserGroupIds());
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < RolePageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<RolePageVO>> page(@Validated RoleSearchDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<RolePageVO> data = this.roleService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 用户所属角色.
     *
     * @param orgId  组织主键（可选）
     * @param userId 用户主键（可选）
     * @return ResultVO<List < RoleBaseVO>>
     */
    @Operation(summary = "用户所属角色")
    @GetMapping("listCheckedByUserId")
    public ResultVO<List<RoleBaseVO>> listCheckedByUserId(
        @RequestParam(required = false) Long orgId,
        @RequestParam(required = false) Long userId) {
        orgId = Optional.ofNullable(orgId).orElse(this.getUserInfo().getOrgId());
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        final List<RoleBaseVO> data = this.roleService.listCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    /**
     * 用户所属角色列表.
     *
     * @param userId 用户主键（可选）
     * @return ResultVO < List < RoleBaseCheckedVO>>
     */
    @Operation(summary = "用户所属角色列表")
    @GetMapping("listByUserId")
    public ResultVO<List<RoleBaseCheckedVO>> listByUserId(@RequestParam(required = false) Long userId) {
        Long orgId = this.getUserInfo().getOrgId();
        List<RoleBaseCheckedVO> data = this.roleService.listByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    /**
     * 用户组所属角色列表.
     *
     * @param userGroupId 用户组主键
     * @return ResultVO < List < RoleBaseCheckedVO>>
     */
    @Operation(summary = "用户组所属角色列表")
    @GetMapping("listByUserGroupId")
    public ResultVO<List<RoleBaseCheckedVO>> listByUserGroupId(@RequestParam(required = false) Long userGroupId) {
        Long orgId = this.getUserInfo().getOrgId();
        List<RoleBaseCheckedVO> data = this.roleService.listByUserGroupId(orgId, userGroupId);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO < RoleExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @GetMapping("getDetailExtend")
    public ResultVO<RoleExtendVO> getDetailExtend(@RequestParam Long id) {
        RoleExtendVO data = this.roleService.getDetailExtend(id);
        return ResultVO.success(data);
    }
}
