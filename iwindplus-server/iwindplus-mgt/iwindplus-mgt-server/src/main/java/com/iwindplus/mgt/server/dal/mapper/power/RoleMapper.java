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
import com.iwindplus.mgt.domain.dto.power.RoleSearchDTO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.domain.vo.power.RoleExtendVO;
import com.iwindplus.mgt.domain.vo.power.RolePageVO;
import com.iwindplus.mgt.server.dal.model.power.RoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色数据访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface RoleMapper extends MPJBaseMapper<RoleDO> {

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<RolePageVO>
     */
    IPage<RolePageVO> selectPageByCondition(PageDTO<RoleDO> page, @Param(Constants.WRAPPER) RoleSearchDTO entity);

    /**
     * 通过组织主键查询.
     *
     * @param orgId 组织主键
     * @return List<RoleBaseCheckedVO>
     */
    List<RoleBaseCheckedVO> selectListByOrgId(@Param("orgId") Long orgId);

    /**
     * 通过用户主键查询.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<RoleBaseCheckedVO>
     */
    List<RoleBaseCheckedVO> selectListByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);

    /**
     * 通过用户组主键查询.
     *
     * @param orgId       组织主键
     * @param userGroupId 用户组主键
     * @return List<RoleBaseCheckedVO>
     */
    List<RoleBaseCheckedVO> selectListByUserGroupId(@Param("orgId") Long orgId, @Param("userGroupId") Long userGroupId);

    /**
     * 通过用户主键查询选中的角色.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<RoleBaseVO>
     */
    List<RoleBaseVO> selectListCheckedByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return RoleExtendVO
     */
    RoleExtendVO selectDetailById(@Param("id") Long id);
}
