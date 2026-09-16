/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.upms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisRateLimiter;
import com.iwindplus.base.util.domain.vo.GoogleAuthVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.query.upms.organization.OrgQueryService;
import com.iwindplus.mgt.application.query.upms.user.UserQueryService;
import com.iwindplus.mgt.application.query.upms.user.vo.UserLoginExtendVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserLoginVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserPageVO;
import com.iwindplus.mgt.application.service.WsPushApplicationService;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgSaveUserDTO;
import com.iwindplus.mgt.application.service.upms.user.UserApplicationService;
import com.iwindplus.mgt.application.service.upms.user.UserGroupUserApplicationService;
import com.iwindplus.mgt.application.service.upms.user.UserPositionApplicationService;
import com.iwindplus.mgt.application.service.upms.user.UserRoleApplicationService;
import com.iwindplus.mgt.application.service.upms.user.dto.EditMailDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditPasswordByMailDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditPasswordByMobileDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.EditPasswordDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserGrantPositionDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserGrantRoleDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserGrantUserGroupDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSaveEditDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSearchDTO;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty.WsConfig;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
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

    private final UserApplicationService userApplicationService;
    private final UserQueryService userQueryService;
    private final UserRoleApplicationService userRoleApplicationService;
    private final UserPositionApplicationService userPositionApplicationService;
    private final UserGroupUserApplicationService userGroupApplicationService;
    private final WsPushApplicationService wsPushApplicationService;
    private final OrgQueryService orgQueryService;
    private final MgtProperty property;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "user", operateType = "save", operateName = "添加", operateDesc = "添加用户")
    public ResultVO<Boolean> save(@RequestBody @Validated(SaveGroup.class) UserSaveEditDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.userApplicationService.save(entity);
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
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "saveOrgUser", operateName = "给组织添加用户", operateDesc = "给组织添加用户")
    public ResultVO<Boolean> saveOrgUser(@RequestBody OrgSaveUserDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        boolean data = this.userApplicationService.saveOrgUser(entity);
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
        boolean data = this.userApplicationService.removeByIds(ids);
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
    @OperateLog(keys = "#entity.id", bizType = "user", operateType = "edit", operateName = "编辑", operateDesc = "编辑用户")
    public ResultVO<Boolean> edit(@RequestBody @Validated(EditGroup.class) UserSaveEditDTO entity) {
        boolean data = this.userApplicationService.edit(entity);
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
    @OperateLog(keys = {"#id"}, bizType = "user", operateType = "editEnabled", operateName = "编辑账号状态", operateDesc = "编辑用户账号状态")
    public ResultVO<Boolean> editEnabled(@RequestParam Long id, @RequestParam Boolean enabled) {
        boolean data = this.userApplicationService.editEnabled(id, enabled);
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
    @OperateLog(keys = {"#id"}, bizType = "user", operateType = "editBuildIn", operateName = "编辑设为内置", operateDesc = "编辑用户账号设为内置")
    public ResultVO<Boolean> editBuildIn(@RequestParam Long id, @RequestParam Boolean buildInFlag) {
        boolean data = this.userApplicationService.editBuildIn(id, buildInFlag);
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
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editPassword", operateName = "编辑密码", operateDesc = "编辑用户密码")
    public ResultVO<Boolean> editPassword(@RequestBody @Validated EditPasswordDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.userApplicationService.editPassword(entity);
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
    @OperateLog(keys = {
        "#entity.mobile"}, bizType = "user", operateType = "editPasswordByMobile", operateName = "编辑密码（手机号方式）", operateDesc = "编辑用户密码（手机号方式）")
    public ResultVO<Boolean> editPasswordByMobile(@RequestBody @Validated EditPasswordByMobileDTO entity) {
        boolean data = this.userApplicationService.editPasswordByMobile(entity);
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
    @OperateLog(keys = {
        "#entity.mail"}, bizType = "user", operateType = "editPasswordByMail", operateName = "编辑密码（邮箱方式）", operateDesc = "编辑用户密码（邮箱方式）")
    public ResultVO<Boolean> editPasswordByMail(@RequestBody @Validated EditPasswordByMailDTO entity) {
        boolean data = this.userApplicationService.editPasswordByMail(entity);
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
    @OperateLog(keys = {"#entity.currentUserId"}, bizType = "user", operateType = "editMail", operateName = "编辑邮箱", operateDesc = "编辑用户邮箱")
    public ResultVO<Boolean> editMail(@RequestBody @Validated EditMailDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.userApplicationService.editMail(entity);
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
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editBatchRole", operateName = "授权角色", operateDesc = "用户授权角色")
    public ResultVO<Boolean> editBatchRole(@RequestBody @Validated UserGrantRoleDTO entity) {
        boolean data = this.userRoleApplicationService.editBatchRole(entity.getUserId(), entity.getRoleIds());

        this.sendWsRolePermission(entity);

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
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editBatchPosition", operateName = "授权职位", operateDesc = "用户授权职位")
    public ResultVO<Boolean> editBatchPosition(@RequestBody @Validated UserGrantPositionDTO entity) {
        boolean data = this.userPositionApplicationService.editBatchPosition(entity.getUserId(), entity.getPositionIds());
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
    @OperateLog(keys = {"#entity.userId"}, bizType = "user", operateType = "editBatchUserGroup", operateName = "授权用户组", operateDesc = "用户授权用户组")
    public ResultVO<Boolean> editBatchUserGroup(@RequestBody @Validated UserGrantUserGroupDTO entity) {
        boolean data = this.userGroupApplicationService.editBatchUserGroup(entity.getUserId(), entity.getUserGroupIds());
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
    @OperateLog(keys = {"#userId"}, bizType = "user", operateType = "editExitOrg", operateName = "退出组织", operateDesc = "退出组织")
    public ResultVO<Boolean> editExitOrg(@RequestParam(required = false) Long userId) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        boolean data = this.userApplicationService.editExitOrg(userId);
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
    @OperateLog(keys = {"#userOrgId"}, bizType = "user", operateType = "editChangeOrg", operateName = "切换组织", operateDesc = "切换组织")
    public ResultVO<Boolean> editChangeOrg(@RequestParam Long userOrgId) {
        Long userId = this.getUserInfo().getUserId();
        boolean data = this.userApplicationService.editChangeOrg(userOrgId, userId);
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
    @OperateLog(bizType = "#userId", operateType = "editGaBindFlag", operateName = "编辑GA绑定状态", operateDesc = "编辑GA绑定状态")
    public ResultVO<Boolean> editGaBindFlag(@RequestParam(required = false) Long userId, @RequestParam String captcha) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        boolean data = this.userApplicationService.editGaBindFlag(userId, captcha);
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
        boolean data = this.userApplicationService.editResetGa(userId);
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
        IPage<UserPageVO> data = this.userQueryService.page(entity);
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
        UserExtendVO data = this.userQueryService.getDetailExtend(userId);
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
        UserLoginExtendVO data = this.userQueryService.getUserExtendInfo(orgId, userId);
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
        List<UserVO> data = this.userQueryService.listByCondition(param);
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
        GoogleAuthVO data = this.userApplicationService.getGaQrcode(userId, width, height);
        return ResultVO.success(data);
    }

    private void sendWsRolePermission(UserGrantRoleDTO entity) {
        final WsConfig ws = this.property.getWs();
        if (Boolean.FALSE.equals(ws.getEnabled())) {
            return;
        }
        if (Boolean.FALSE.equals(ws.getEnabledRolePermission())) {
            return;
        }

        final UserBaseVO userInfo = this.getUserInfo();
        this.wsPushApplicationService.sendWsRolePermission(entity.getUserId(), this.orgQueryService.getOrgId(entity.getUserId())
            , userInfo.getOrgId(), userInfo.getUserId());
    }
}
