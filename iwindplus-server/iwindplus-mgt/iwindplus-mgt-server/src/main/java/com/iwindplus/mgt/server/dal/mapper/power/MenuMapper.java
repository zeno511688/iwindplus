/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.BaseTreeVO;
import com.iwindplus.mgt.domain.vo.power.MenuBaseListSystemVO;
import com.iwindplus.mgt.domain.vo.power.MenuListSystemVO;
import com.iwindplus.mgt.server.dal.model.power.MenuDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 菜单数据访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface MenuMapper extends MPJBaseMapper<MenuDO> {

    /**
     * 查询启用的.
     *
     * @return List<MenuBaseListSystemVO>
     */
    List<MenuBaseListSystemVO> selectListEnabled();

    /**
     * 通过角色主键查询选中的.
     *
     * @param orgId  组织主键
     * @param roleId 角色主键
     * @return List<MenuBaseListSystemVO>
     */
    List<MenuBaseListSystemVO> selectListByRoleId(@Param("orgId") Long orgId, @Param("roleId") Long roleId);

    /**
     * 通过系统主键查询.
     *
     * @param systemId 系统主键
     * @param status   状态
     * @return List<BaseTreeVO>
     */
    List<BaseTreeVO> selectListBySystemId(
        @Param("systemId") Long systemId,
        @Param("status") EnableStatusEnum status);

    /**
     * 通过用户主键查询选中的.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<MenuListSystemVO>
     */
    List<MenuListSystemVO> selectListByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);

}