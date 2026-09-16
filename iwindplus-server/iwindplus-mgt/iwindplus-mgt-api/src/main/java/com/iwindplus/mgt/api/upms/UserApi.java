/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.upms;

import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.api.upms.vo.UserDepartmentInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserDetailVO;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.api.upms.vo.UserInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserOrgInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24
 */
public interface UserApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/user/";

    /**
     * 批量查询.
     *
     * @param ids 主键集合
     * @return ResultVO<List < UserVO>>
     */
    @Operation(summary = "批量查询")
    @GetMapping(API_PREFIX + "listInfoByIds")
    ResultVO<List<UserVO>> listInfoByIds(@RequestParam(value = "ids") List<Long> ids);

    /**
     * 批量查询（扩展）.
     *
     * @param ids 主键集合
     * @return ResultVO<List < UserExtendVO>>
     */
    @Operation(summary = "批量查询（扩展）")
    @GetMapping(API_PREFIX + "listExtendByIds")
    ResultVO<List<UserExtendVO>> listExtendByIds(@RequestParam(value = "ids") List<Long> ids);

    /**
     * 通过角色主键集合查询用户组织信息.
     *
     * @param roleIds 角色主键集合
     * @return ResultVO<List < UserOrgInfoVO>>
     */
    @Operation(summary = "通过角色主键集合查询用户组织信息")
    @GetMapping(API_PREFIX + "listByRoleIds")
    ResultVO<List<UserOrgInfoVO>> listByRoleIds(@RequestParam(value = "roleIds") List<Long> roleIds);

    /**
     * 通过部门主键集合查询用户组织信息.
     *
     * @param departmentIds 部门主键集合
     * @return ResultVO<List < UserDepartmentInfoVO>>
     */
    @Operation(summary = "通过部门主键集合查询用户组织信息")
    @GetMapping(API_PREFIX + "listByDepartmentIds")
    ResultVO<List<UserDepartmentInfoVO>> listByDepartmentIds(@RequestParam(value = "departmentIds") List<Long> departmentIds);

    /**
     * 用户扩展功能校验是否正确.
     *
     * @param entity 用户主键
     * @return ResultVO<UserCaptchaValidVO>
     */
    @Operation(summary = "用户扩展功能校验是否正确")
    @PostMapping(API_PREFIX + "checkExtendFunctionByUserId")
    ResultVO<UserExtendFunctionValidVO> checkExtendFunctionByUserId(@RequestBody @Validated UserExtendFunctionValidDTO entity);

    /**
     * 通过条件查询.
     *
     * @param entity 对象
     * @return ResultVO<UserVO>
     */
    @Operation(summary = "通过条件查询")
    @PostMapping(API_PREFIX + "getByCondition")
    ResultVO<UserVO> getByCondition(@RequestBody @Validated UserBaseQueryDTO entity);

    /**
     * 通过条件查询登录信息.
     *
     * @param entity 条件
     * @return ResultVO<UserInfoVO>
     */
    @Operation(summary = "通过条件查询登录信息")
    @PostMapping(API_PREFIX + "getLoginInfoByCondition")
    ResultVO<UserInfoVO> getLoginInfoByCondition(@RequestBody @Validated UserBaseQueryDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < UserVO>
     */
    @Operation(summary = "详情")
    @GetMapping(API_PREFIX + "getDetail")
    ResultVO<UserVO> getDetail(@RequestParam(value = "id") Long id);

    /**
     * 用户登录（支持用户名/手机/邮箱/身份证）.
     *
     * @param param 参数
     * @return ResultVO<UserDetailVO>
     */
    @Operation(summary = "用户登录（支持用户名/手机/邮箱/身份证）")
    @GetMapping(API_PREFIX + "getLoginByParam")
    ResultVO<UserDetailVO> getLoginByParam(@RequestParam(value = "param") String param);

    /**
     * 用户登录（支持唯一编码，用于绑定授权方式，如微信公众号，小程序等）.
     *
     * @param code 编码
     * @return ResultVO<UserDetailVO>
     */
    @Operation(summary = "用户登录（支持唯一编码，用于绑定授权方式，如微信公众号，小程序等）")
    @GetMapping(API_PREFIX + "getLoginByCode")
    ResultVO<UserDetailVO> getLoginByCode(@RequestParam(value = "code") String code);

    /**
     * 获取用户oss访问路径（路径集合为头像等）.
     *
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（单位：分钟，默认：60）
     * @return ResultVO<List < FilePathVO>>
     */
    @Operation(summary = "获取用户oss访问路径（路径集合为头像等）")
    @GetMapping(API_PREFIX + "listUserOssSignUrl")
    ResultVO<List<FilePathVO>> listUserOssSignUrl(
        @RequestParam(value = "relativePaths") List<String> relativePaths,
        @RequestParam(value = "timeout", required = false) Integer timeout);
}
