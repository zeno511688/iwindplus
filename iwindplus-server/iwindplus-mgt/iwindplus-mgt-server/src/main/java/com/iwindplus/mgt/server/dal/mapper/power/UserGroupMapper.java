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
import com.iwindplus.mgt.domain.dto.power.UserGroupSearchDTO;
import com.iwindplus.mgt.domain.vo.power.UserGroupBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupPageVO;
import com.iwindplus.mgt.server.dal.model.power.UserGroupDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户组数据访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface UserGroupMapper extends MPJBaseMapper<UserGroupDO> {

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<UserGroupPageVO>
     */
    IPage<UserGroupPageVO> selectPageByCondition(PageDTO<UserGroupDO> page, @Param(Constants.WRAPPER) UserGroupSearchDTO entity);

    /**
     * 通过组织主键查询.
     *
     * @param orgId 组织主键
     * @return List<UserGroupBaseCheckedVO>
     */
    List<UserGroupBaseCheckedVO> selectListByOrgId(@Param("orgId") Long orgId);

    /**
     * 通过用户主键查询选中的组.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<UserGroupBaseCheckedVO>
     */
    List<UserGroupBaseCheckedVO> selectListByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);

    /**
     * 通过角色主键查询选中的组.
     *
     * @param orgId  组织主键
     * @param roleId 角色主键
     * @return List<UserGroupBaseCheckedVO>
     */
    List<UserGroupBaseCheckedVO> selectListByRoleId(@Param("orgId") Long orgId, @Param("roleId") Long roleId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return UserGroupExtendVO
     */
    UserGroupExtendVO selectDetailById(@Param("id") Long id);
}
