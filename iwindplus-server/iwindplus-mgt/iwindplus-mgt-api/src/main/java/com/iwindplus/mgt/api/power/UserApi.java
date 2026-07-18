/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.api.power;

import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.vo.power.UserDepartmentInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserDetailVO;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserOrgInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
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
     * 用户登录（支持唯一编码，用于第三方绑定授权方式，如微信公众号，小程序等）.
     *
     * @param code 编码
     * @return ResultVO<UserDetailVO>
     */
    @Operation(summary = "用户登录（支持唯一编码，用于第三方绑定授权方式，如微信公众号，小程序等）")
    @GetMapping(API_PREFIX + "getLoginByCode")
    ResultVO<UserDetailVO> getLoginByCode(@RequestParam(value = "code") String code);

}
