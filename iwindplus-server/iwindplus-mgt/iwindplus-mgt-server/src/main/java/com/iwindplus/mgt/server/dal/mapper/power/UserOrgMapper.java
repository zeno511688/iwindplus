/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.server.dal.model.power.UserOrgDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户组织关系访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface UserOrgMapper extends MPJBaseMapper<UserOrgDO> {

    /**
     * 真实删除.
     *
     * @param ids 主键集合
     * @return int
     */
    int deleteByIds(@Param(Constants.LIST) List<Long> ids);

    /**
     * 通过用户主键真实删除.
     *
     * @param userIds 用户主键集合
     * @return int
     */
    int deleteByUserIds(@Param(Constants.LIST) List<Long> userIds);

    /**
     * 通过组织主键真实删除.
     *
     * @param orgIds 组织主键集合
     * @return int
     */
    int deleteByOrgIds(@Param(Constants.LIST) List<Long> orgIds);
}
