/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.system;

import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.server.dal.model.system.ServerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务数据访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface ServerMapper extends MPJBaseMapper<ServerDO> {

    /**
     * 通过上下文表达式查找.
     *
     * @param pattern 上下文表达式
     * @return long
     */
    long selectCountByPattern(@Param("pattern") String pattern);
}
