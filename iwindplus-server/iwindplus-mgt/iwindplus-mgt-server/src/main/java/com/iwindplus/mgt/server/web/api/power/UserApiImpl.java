/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.power;

import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.mgt.api.power.UserApi;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.vo.power.UserDepartmentInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserDetailVO;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserOrgInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import com.iwindplus.mgt.server.service.power.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final UserService userService;

    @Override
    public ResultVO<List<UserVO>> listInfoByIds(List<Long> ids) {
        List<UserVO> data = this.userService.listInfoByIds(ids);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<UserExtendVO>> listExtendByIds(List<Long> ids) {
        List<UserExtendVO> data = this.userService.listExtendByIds(ids);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<UserOrgInfoVO>> listByRoleIds(List<Long> roleIds) {
        List<UserOrgInfoVO> data = this.userService.listByRoleIds(roleIds);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<UserDepartmentInfoVO>> listByDepartmentIds(List<Long> departmentIds) {
        List<UserDepartmentInfoVO> data = this.userService.listByDepartmentIds(departmentIds);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserExtendFunctionValidVO> checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity) {
        UserExtendFunctionValidVO data = this.userService.checkExtendFunctionByUserId(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserVO> getByCondition(UserBaseQueryDTO entity) {
        UserVO data = this.userService.getByCondition(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserInfoVO> getLoginInfoByCondition(UserBaseQueryDTO entity) {
        UserInfoVO data = this.userService.getLoginInfoByCondition(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserVO> getDetail(Long id) {
        UserVO data = this.userService.getDetail(id);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserDetailVO> getLoginByParam(String param) {
        UserDetailVO data = this.userService.getLoginByParam(param);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserDetailVO> getLoginByCode(String code) {
        UserDetailVO data = this.userService.getLoginByCode(code);
        return ResultVO.success(data);
    }
}
