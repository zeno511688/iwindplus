/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.dto.power.UserSearchDTO;
import com.iwindplus.mgt.domain.vo.power.UserDepartmentInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserDetailVO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserOrgInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserPageVO;
import com.iwindplus.mgt.server.dal.model.power.UserDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface UserMapper extends MPJBaseMapper<UserDO> {

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<UserPageVO>
     */
    IPage<UserPageVO> selectPageByCondition(PageDTO<UserPageVO> page, @Param(Constants.WRAPPER) UserSearchDTO entity);

    /**
     * 通过角色主键集合查询用户组织信息.
     *
     * @param roleIds 角色主键集合
     * @return List<UserOrgInfoVO>
     */
    List<UserOrgInfoVO> selectListByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 通过部门主键集合查询用户部门信息.
     *
     * @param departmentIds 部门主键集合
     * @return List<UserDepartmentInfoVO>
     */
    List<UserDepartmentInfoVO> selectListByDepartmentIds(@Param("departmentIds") List<Long> departmentIds);

    /**
     * 用户登录（支持用户名/手机/邮箱/身份证）.
     *
     * @param param 参数
     * @return UserDetailVO
     */
    UserDetailVO selectLoginByParam(@Param("param") String param);

    /**
     * 用户登录（支持唯一编码，用于第三方绑定授权方式）.
     *
     * @param code 编码
     * @return UserDetailVO
     */
    UserDetailVO selectLoginByCode(@Param("code") String code);

    /**
     * 通过用户主键查询.
     *
     * @param userId 用户主键
     * @return UserInfoVO
     */
    UserInfoVO selectByUserId(@Param("userId") Long userId);

    /**
     * 通过条件查询登录信息.
     *
     * @param entity 条件
     * @return UserInfoVO
     */
    UserInfoVO selectByCondition(@Param(Constants.WRAPPER) UserBaseQueryDTO entity);
}
