/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.service;

import com.iwindplus.base.document.domain.dto.ExportTaskDTO;
import com.iwindplus.base.document.domain.dto.ExportTaskShardSearchDTO;
import com.iwindplus.base.document.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.document.domain.vo.ExportTaskVO;
import java.util.List;

/**
 * 导出任务Service.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
public interface ExportTaskService {

    /**
     * 获取每页条数.
     *
     * @return 每页条数
     */
    Integer getSize();

    /**
     * 创建导出任务.
     *
     * @param entity 对象
     * @return ExportTaskVO
     */
    ExportTaskVO save(ExportTaskDTO entity);

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    boolean editStatusById(ExportTaskStatusEditDTO entity);

    /**
     * 分片查询导出任务列表.
     *
     * @param param 查询参数
     * @return 导出任务列表
     */
    List<ExportTaskVO> listByShard(ExportTaskShardSearchDTO param);

    /**
     * 根据ID查询导出任务.
     *
     * @param id 主键
     * @return ExportTaskVO
     */
    ExportTaskVO getDetail(Long id);

    /**
     * 通过业务流水号查找.
     *
     * @param bizNumber 业务流水号
     * @return ExportTaskVO
     */
    ExportTaskVO getDetailByBizNumber(String bizNumber);
}
