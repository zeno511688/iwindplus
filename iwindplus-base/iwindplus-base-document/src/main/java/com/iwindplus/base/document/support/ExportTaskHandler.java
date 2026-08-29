/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.document.domain.vo.ExportTaskVO;
import com.iwindplus.base.domain.dto.DbPageDTO;

/**
 * 导出任务处理器.
 *
 * @param <Q> 查询参数
 * @param <V> 返回值
 * @author zengdegui
 * @since 2026/08/27
 */
public interface ExportTaskHandler<Q extends DbPageDTO, V> {

    /**
     * 获取执行器名称（有默认值不需要实现）.
     *
     * @return 执行器名称
     */
    default String getExecuteName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取查询参数的Class对象.
     *
     * @return Class对象
     */
    Class<Q> getQueryClass();

    /**
     * 获取导出数据的Class对象（行数据类型）.
     *
     * @return Class对象
     */
    Class<V> getRowClass();

    /**
     * 获取导出数据的Sheet名称（有默认值不需要实现）.
     *
     * @return Sheet名称
     */
    default String getSheetName() {
        return "Sheet1";
    }

    /**
     * 按条件分页查询数据.
     *
     * @param queryParam 查询参数类型
     * @return 分页数据
     */
    PageDTO<V> pageByCondition(Q queryParam);

    /**
     * 任务成功.
     *
     * @param entity 命令对象
     */
    default void onTaskSuccess(ExportTaskVO entity) {
    }

    /**
     * 导出任务失败.
     *
     * @param entity 命令对象
     */
    default void onTaskFail(ExportTaskVO entity) {
    }

}
