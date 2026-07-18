/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.BaseTreeCheckedVO;
import com.iwindplus.mgt.domain.vo.power.DepartmentBaseVO;
import com.iwindplus.mgt.domain.vo.power.DepartmentExtendVO;
import com.iwindplus.mgt.server.dal.model.power.DepartmentDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Mapper
public interface DepartmentMapper extends MPJBaseMapper<DepartmentDO> {

    /**
     * 通过组织主键查询.
     *
     * @param orgId  组织主键
     * @param status 状态
     * @return List<BaseTreeCheckedVO>
     */
    List<BaseTreeCheckedVO> selectListByOrgId(@Param("orgId") Long orgId, @Param("status") EnableStatusEnum status);

    /**
     * 详情.
     *
     * @param id 主键
     * @return DepartmentExtendVO
     */
    DepartmentExtendVO selectDetailById(@Param("id") Long id);

    /**
     * 通过用户主键查询.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<DepartmentBaseVO>
     */
    List<DepartmentBaseVO> selectListCheckedByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);
}
