/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.redis.domain.annotation.RedisRateLimiter;
import com.iwindplus.base.util.domain.vo.GoogleAuthVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.power.EditMailDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordByMailDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordByMobileDTO;
import com.iwindplus.mgt.domain.dto.power.EditPasswordDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSaveUserDTO;
import com.iwindplus.mgt.domain.dto.power.UserGrantPositionDTO;
import com.iwindplus.mgt.domain.dto.power.UserGrantRoleDTO;
import com.iwindplus.mgt.domain.dto.power.UserGrantUserGroupDTO;
import com.iwindplus.mgt.domain.dto.power.UserSaveEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserSearchDTO;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserLoginExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserLoginVO;
import com.iwindplus.mgt.domain.vo.power.UserPageVO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import com.iwindplus.mgt.server.service.WsPushService;
import com.iwindplus.mgt.server.service.power.OrgService;
import com.iwindplus.mgt.server.service.power.UserGroupUserService;
import com.iwindplus.mgt.server.service.power.UserPositionService;
import com.iwindplus.mgt.server.service.power.UserRoleService;
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
 * 用户相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "用户接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/user")
@Validated
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final UserPositionService userPositionService;
    private final UserGroupUserService userGroupService;
    private final WsPushService wsPushService;
    private final OrgService orgService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "user", operateType = "save", operateName = "添加", operateDesc = "添加用户")
    public ResultVO<Boolean> save(@RequestBody @Validated(SaveGroup.class) UserSaveEditDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.userService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 给组织添加用户.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "给组织添加用户")
    @PostMapping("saveOrgUser")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "saveOrgUser", operateName = "给组织添加用户", operateDesc = "给组织添加用户")
    public ResultVO<Boolean> saveOrgUser(@RequestBody OrgSaveUserDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.userService.saveOrgUser(entity);
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
    @OperateLog(bizType = "user", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除用户")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.userService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑用户")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "user", operateType = "edit", operateName = "编辑", operateDesc = "编辑用户")
    public ResultVO<Boolean> edit(@RequestBody @Validated(EditGroup.class) UserSaveEditDTO entity) {
        boolean data = this.userService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑账号状态.
     *
     * @param id      主键
     * @param enabled 账号状态
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑账号状态")
    @PutMapping("editEnabled")
    @RedisIdempotent
    @OperateLog(keys = {"#id"}, bizType = "user", operateType = "editEnabled", operateName = "编辑账号状态", operateDesc = "编辑用户账号状态")
    public ResultVO<Boolean> editEnabled(@RequestParam Long id, @RequestParam Boolean enabled) {
        boolean data = this.userService.editEnabled(id, enabled);
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
    @OperateLog(keys = {"#id"}, bizType = "user", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑用户账号设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.userService.editBuildIn(id, buildInFlag);
        return ResultVO.success(data);
    }

    /**
     * 编辑密码.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑密码")
    @PutMapping("editPassword")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editPassword", operateName = "编辑密码", operateDesc = "编辑用户密码")
    public ResultVO<Boolean> editPassword(@RequestBody @Validated EditPasswordDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.userService.editPassword(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑密码（手机号方式）.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑密码（手机号方式）")
    @PutMapping("editPasswordByMobile")
    @RedisIdempotent
    @OperateLog(keys = {
        "#entity.mobile"}, bizType = "user", operateType = "editPasswordByMobile", operateName = "编辑密码（手机号方式）", operateDesc = "编辑用户密码（手机号方式）")
    public ResultVO<Boolean> editPasswordByMobile(@RequestBody @Validated EditPasswordByMobileDTO entity) {
        boolean data = this.userService.editPasswordByMobile(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑密码（邮箱方式）.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑密码（邮箱方式）")
    @PutMapping("editPasswordByMail")
    @RedisIdempotent
    @OperateLog(keys = {
        "#entity.mail"}, bizType = "user", operateType = "editPasswordByMail", operateName = "编辑密码（邮箱方式）", operateDesc = "编辑用户密码（邮箱方式）")
    public ResultVO<Boolean> editPasswordByMail(@RequestBody @Validated EditPasswordByMailDTO entity) {
        boolean data = this.userService.editPasswordByMail(entity);
        return ResultVO.success(data);
    }

    /**
     * 编辑邮箱.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑邮箱")
    @PutMapping("editMail")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.currentUserId"}, bizType = "user", operateType = "editMail", operateName = "编辑邮箱", operateDesc = "编辑用户邮箱")
    public ResultVO<Boolean> editMail(@RequestBody @Validated EditMailDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.userService.editMail(entity);
        return ResultVO.success(data);
    }

    /**
     * 授权角色.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "授权角色")
    @PutMapping("editBatchRole")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editBatchRole", operateName = "授权角色", operateDesc = "用户授权角色")
    public ResultVO<Boolean> editBatchRole(@RequestBody @Validated UserGrantRoleDTO entity) {
        boolean data = this.userRoleService.editBatchRole(entity.getUserId(), entity.getRoleIds());

        final UserBaseVO userInfo = this.getUserInfo();
        this.wsPushService.sendWsRolePermission(entity.getUserId(), this.orgService.getOrgId(entity.getUserId())
            , userInfo.getOrgId(), userInfo.getUserId());

        return ResultVO.success(data);
    }

    /**
     * 授权职位.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "授权职位")
    @PutMapping("editBatchPosition")
    @RedisIdempotent
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editBatchPosition", operateName = "授权职位", operateDesc = "用户授权职位")
    public ResultVO<Boolean> editBatchPosition(@RequestBody @Validated UserGrantPositionDTO entity) {
        boolean data = this.userPositionService.editBatchPosition(entity.getUserId(), entity.getPositionIds());
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
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editBatchUserGroup", operateName = "授权用户组", operateDesc = "用户授权用户组")
    public ResultVO<Boolean> editBatchUserGroup(@RequestBody @Validated UserGrantUserGroupDTO entity) {
        boolean data = this.userGroupService.editBatchUserGroup(entity.getUserId(), entity.getUserGroupIds());
        return ResultVO.success(data);
    }

    /**
     * 退出组织.
     *
     * @param userId 用户主键
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "退出组织")
    @DeleteMapping("editExitOrg")
    @RedisIdempotent
    @OperateLog(keys = {"#userId"}, bizType = "user", operateType = "editExitOrg", operateName = "退出组织", operateDesc = "退出组织")
    public ResultVO<Boolean> editExitOrg(@RequestParam(required = false) Long userId) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        boolean data = this.userService.editExitOrg(userId);
        return ResultVO.success(data);
    }

    /**
     * 切换组织.
     *
     * @param userOrgId 用户组织关系主键（可选）
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "切换组织")
    @PutMapping("editChangeOrg")
    @RedisIdempotent
    @OperateLog(keys = {"#userOrgId"}, bizType = "user", operateType = "editChangeOrg", operateName = "切换组织", operateDesc = "切换组织")
    public ResultVO<Boolean> editChangeOrg(@RequestParam Long userOrgId) {
        Long userId = this.getUserInfo().getUserId();
        boolean data = this.userService.editChangeOrg(userOrgId, userId);
        return ResultVO.success(data);
    }

    /**
     * 编辑GA绑定状态.
     *
     * @param userId  用户主键
     * @param captcha 验证码
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑GA绑定状态")
    @PutMapping("editGaBindFlag")
    @RedisIdempotent
    @OperateLog(bizType = "#userId", operateType = "editGaBindFlag", operateName = "编辑GA绑定状态", operateDesc = "编辑GA绑定状态")
    public ResultVO<Boolean> editGaBindFlag(@RequestParam(required = false) Long userId, @RequestParam String captcha) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        boolean data = this.userService.editGaBindFlag(userId, captcha);
        return ResultVO.success(data);
    }

    /**
     * 编辑重置GA.
     *
     * @param userId 用户主键
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑重置GA")
    @PutMapping("editResetGa")
    @OperateLog(bizType = "#userId", operateType = "editResetGa", operateName = "编辑重置GA", operateDesc = "编辑重置GA")
    public ResultVO<Boolean> editResetGaSecret(@RequestParam(required = false) Long userId) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        boolean data = this.userService.editResetGa(userId);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < UserPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<UserPageVO>> page(@Validated UserSearchDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<UserPageVO> data = this.userService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResultVO<UserExtendVO>
     */
    @Operation(summary = "详情（扩展）")
    @RedisRateLimiter
    @GetMapping("getDetailExtend")
    public ResultVO<UserExtendVO> getDetailExtend(@RequestParam(required = false) Long id) {
        Long userId = Optional.ofNullable(id).orElse(this.getUserInfo().getUserId());
        UserExtendVO data = this.userService.getDetailExtend(userId);
        return ResultVO.success(data);
    }

    /**
     * 获取登录用户信息.
     *
     * @param orgId  组织主键（可选）
     * @param userId 用户主键（可选）
     * @return ResultVO<UserLoginVO>
     */
    @Operation(summary = "获取登录用户信息")
    @GetMapping(value = "getUserInfo")
    public ResultVO<UserLoginVO> getUserInfo(
        @RequestParam(required = false) Long orgId,
        @RequestParam(required = false) Long userId) {
        orgId = Optional.ofNullable(orgId).orElse(this.getUserInfo().getOrgId());
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        UserLoginExtendVO data = this.userService.getUserExtendInfo(orgId, userId);
        return ResultVO.success(data);
    }

    /**
     * 通过条件模糊搜索.
     *
     * @param param 参数
     * @return ResultVO<List < UserVO>>
     */
    @Operation(summary = "通过条件模糊搜索")
    @GetMapping("listByCondition")
    public ResultVO<List<UserVO>> listByCondition(@RequestParam String param) {
        List<UserVO> data = this.userService.listByCondition(param);
        return ResultVO.success(data);
    }

    /**
     * 获取GA二维码.
     *
     * @param width  宽度
     * @param height 高度
     * @return ResultVO < GoogleAuthVO>
     */
    @Operation(summary = "获取GA二维码")
    @GetMapping("getGaQrcode")
    public ResultVO<GoogleAuthVO> getGaQrcode(@RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) {
        Long userId = this.getUserInfo().getUserId();
        GoogleAuthVO data = this.userService.getGaQrcode(userId, width, height);
        return ResultVO.success(data);
    }
}
