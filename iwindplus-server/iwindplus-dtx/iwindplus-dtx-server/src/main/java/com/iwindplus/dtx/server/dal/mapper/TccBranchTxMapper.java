/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwindplus.dtx.server.dal.model.TccBranchTxDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * tcc分支事务数据访问层接口类.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Mapper
public interface TccBranchTxMapper extends BaseMapper<TccBranchTxDO> {

    /**
     * 真实删除.
     *
     * @param xid 全局事务ID
     * @return int
     */
    int deleteByXid(@Param("xid") String xid);
}
