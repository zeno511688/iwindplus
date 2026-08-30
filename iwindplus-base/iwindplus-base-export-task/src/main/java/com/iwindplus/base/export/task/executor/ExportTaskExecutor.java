/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.executor;

import com.iwindplus.base.export.task.domain.dto.ExportTaskSubmitDTO;
import com.iwindplus.base.export.task.domain.vo.ExportTaskSubmitVO;

/**
 * 导出任务执行器接口.
 *
 * @author zengdegui
 * @since 2026/08/29
 */
public interface ExportTaskExecutor {

    /**
     * 提交导出任务.
     *
     * @param entity 对象
     * @return ExportTaskSubmitVO
     */
    ExportTaskSubmitVO submit(ExportTaskSubmitDTO entity);

    /**
     * 通过主键人工触发重试（只支持废弃的数据）.
     *
     * @param id 主键
     */
    void retryById(Long id);

    /**
     * 通过业务流水号人工触发重试（只支持废弃的数据）.
     *
     * @param bizNumber 业务流水号
     */
    void retryByBizNumber(String bizNumber);
}
