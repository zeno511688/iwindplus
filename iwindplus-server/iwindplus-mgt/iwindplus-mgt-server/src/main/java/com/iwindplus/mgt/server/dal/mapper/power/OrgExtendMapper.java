/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.server.dal.model.power.OrgExtendDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组织扩展数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Mapper
public interface OrgExtendMapper extends MPJBaseMapper<OrgExtendDO> {

    /**
     * 通过组织主键真实删除.
     *
     * @param orgIds 组织主键集合
     * @return int
     */
    int deleteByOrgIds(@Param(Constants.LIST) List<Long> orgIds);
}
