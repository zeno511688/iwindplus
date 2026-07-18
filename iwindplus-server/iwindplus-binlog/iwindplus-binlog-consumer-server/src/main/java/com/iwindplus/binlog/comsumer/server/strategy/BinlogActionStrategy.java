/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.strategy;

import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionProcessDTO;

/**
 * binlog 操作策略.
 *
 * @param <T> 参数
 * @param <R> 结果
 * @author zengdegui
 * @since 2025/09/21 20:18
 */
public interface BinlogActionStrategy<T, R> {

    /**
     * 获取支持的操作类型.
     *
     * @return DbActionTypeEnum
     */
    DbActionTypeEnum support();

    /**
     * 执行业务.
     *
     * @param entity 对象
     * @return R
     */
    R execute(BinlogActionProcessDTO<T> entity);
}
