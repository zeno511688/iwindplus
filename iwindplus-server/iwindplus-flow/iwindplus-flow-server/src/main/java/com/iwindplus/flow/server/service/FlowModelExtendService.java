/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.iwindplus.flow.domain.dto.FlowModelExtendDTO;
import java.util.List;

/**
 * 流程模型扩展业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowModelExtendService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowModelExtendDTO entity);

    /**
     * 通过模型主键真实删除.
     *
     * @param modelIds 模型主键集合
     * @return boolean
     */
    boolean removeByModelIds(List<Long> modelIds);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(FlowModelExtendDTO entity);
}
