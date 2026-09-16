/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.support;

import com.iwindplus.base.domain.constant.CommonConstant.ExcelConstant;
import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;

/**
 * 导出任务处理器.
 *
 * @param <Q> 查询参数对象
 * @param <V> 查询返回值对象
 * @param <E> 导出映射对象
 * @author zengdegui
 * @since 2026/08/27
 */
public interface ExportTaskHandler<Q extends DbPageDTO, V, E> {

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
    Class<E> getRowClass();

    /**
     * 获取导出文件名.
     *
     * @return 文件名
     */
    String getFileName();

    /**
     * 获取导出数据的Sheet名称（有默认值不需要实现）.
     *
     * @return Sheet名称
     */
    default String getSheetName() {
        return ExcelConstant.DEFAULT_SHEET_NAME;
    }

    /**
     * 按条件分页查询数据.
     *
     * @param entity 查询参数
     * @return 分页数据
     */
    DbPageVO<V> pageByCondition(Q entity);

    /**
     * 任务成功.
     *
     * @param entity 对象
     */
    default void onTaskSuccess(ExportTaskVO entity) {
    }

    /**
     * 导出任务失败.
     *
     * @param entity 对象
     */
    default void onTaskFail(ExportTaskVO entity) {
    }
}
