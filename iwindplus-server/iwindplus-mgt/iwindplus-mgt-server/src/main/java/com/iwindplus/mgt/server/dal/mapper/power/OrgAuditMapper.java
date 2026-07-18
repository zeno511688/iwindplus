/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.domain.vo.power.OrgAuditVO;
import com.iwindplus.mgt.server.dal.model.power.OrgAuditDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组织审核数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Mapper
public interface OrgAuditMapper extends MPJBaseMapper<OrgAuditDO> {

    /**
     * 通过组织主键真实删除.
     *
     * @param orgIds 组织主键集合
     * @return int
     */
    int deleteByOrgIds(@Param(Constants.LIST) List<Long> orgIds);

    /**
     * 通过组织主键查询最新记录.
     *
     * @param orgId 组织主键
     * @return OrgAuditVO
     */
    OrgAuditVO selectNewestByOrgId(@Param("orgId") Long orgId);

    /**
     * 通过组织主键查询.
     *
     * @param orgId 组织主键
     * @return List<OrgAuditVO>
     */
    List<OrgAuditVO> selectListByOrgId(@Param("orgId") Long orgId);
}
