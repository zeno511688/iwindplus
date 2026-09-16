/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.upms;

import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;
import com.iwindplus.mgt.api.upms.UserApi;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.api.upms.vo.UserDepartmentInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserDetailVO;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.api.upms.vo.UserInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserOrgInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
import com.iwindplus.mgt.application.query.upms.user.UserQueryService;
import com.iwindplus.mgt.application.service.upms.user.UserApplicationService;
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

    private final UserApplicationService userApplicationService;
    private final UserQueryService userQueryService;

    @Override
    public ResultVO<List<UserVO>> listInfoByIds(List<Long> ids) {
        List<UserVO> data = this.userQueryService.listInfoByIds(ids);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<UserExtendVO>> listExtendByIds(List<Long> ids) {
        List<UserExtendVO> data = this.userQueryService.listExtendByIds(ids);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<UserOrgInfoVO>> listByRoleIds(List<Long> roleIds) {
        List<UserOrgInfoVO> data = this.userQueryService.listByRoleIds(roleIds);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<UserDepartmentInfoVO>> listByDepartmentIds(List<Long> departmentIds) {
        List<UserDepartmentInfoVO> data = this.userQueryService.listByDepartmentIds(departmentIds);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserExtendFunctionValidVO> checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity) {
        UserExtendFunctionValidVO data = this.userQueryService.checkExtendFunctionByUserId(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserVO> getByCondition(UserBaseQueryDTO entity) {
        UserVO data = this.userQueryService.getByCondition(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserInfoVO> getLoginInfoByCondition(UserBaseQueryDTO entity) {
        UserInfoVO data = this.userQueryService.getLoginInfoByCondition(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserVO> getDetail(Long id) {
        UserVO data = this.userQueryService.getDetail(id);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserDetailVO> getLoginByParam(String param) {
        UserDetailVO data = this.userQueryService.getLoginByParam(param);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<UserDetailVO> getLoginByCode(String code) {
        UserDetailVO data = this.userQueryService.getLoginByCode(code);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<FilePathVO>> listUserOssSignUrl(List<String> relativePaths, Integer timeout) {
        List<FilePathVO> data = this.userQueryService.listUserOssSignUrl(relativePaths, timeout);
        return ResultVO.success(data);
    }
}
