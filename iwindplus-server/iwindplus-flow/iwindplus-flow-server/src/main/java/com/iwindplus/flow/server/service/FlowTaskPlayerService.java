/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.iwindplus.flow.domain.dto.FlowTaskPlayerEditDTO;
import com.iwindplus.flow.domain.dto.FlowTaskPlayerSaveDTO;
import java.util.List;

/**
 * 流程任务参与人业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowTaskPlayerService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowTaskPlayerSaveDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(FlowTaskPlayerEditDTO entity);
}
