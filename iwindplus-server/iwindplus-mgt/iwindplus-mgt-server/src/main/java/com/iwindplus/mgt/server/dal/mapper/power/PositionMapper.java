/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.domain.vo.power.PositionBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.PositionExtendVO;
import com.iwindplus.mgt.server.dal.model.power.PositionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 职位数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Mapper
public interface PositionMapper extends MPJBaseMapper<PositionDO> {

    /**
     * 通过组织主键查询.
     *
     * @param orgId         组织主键
     * @param departmentIds 部门主键集合
     * @return List<PositionBaseCheckedVO>
     */
    List<PositionBaseCheckedVO> selectListByOrgId(@Param("orgId") Long orgId, @Param(Constants.LIST) List<Long> departmentIds);

    /**
     * 通过用户主键查询
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param departmentIds 部门主键集合
     * @return List<PositionBaseCheckedVO>
     */
    List<PositionBaseCheckedVO> selectListByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId,
        @Param(Constants.LIST) List<Long> departmentIds);

    /**
     * 详情.
     *
     * @param id 主键
     * @return PositionExtendVO
     */
    PositionExtendVO selectDetailById(@Param("id") Long id);
}
