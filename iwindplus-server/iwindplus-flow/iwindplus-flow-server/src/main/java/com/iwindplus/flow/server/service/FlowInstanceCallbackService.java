/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.iwindplus.flow.domain.dto.FlowInstanceCallbackEditDTO;
import com.iwindplus.flow.domain.dto.FlowInstanceCallbackSaveDTO;
import com.iwindplus.flow.server.dal.model.FlowInstanceCallbackDO;
import java.util.List;

/**
 * 流程实例回调业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowInstanceCallbackService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowInstanceCallbackSaveDTO entity);

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
    boolean edit(FlowInstanceCallbackEditDTO entity);

    /**
     * 执行回调（包含HTTP请求和状态更新）.
     *
     * @param callback 回调记录
     * @return 是否成功
     */
    boolean executeCallback(FlowInstanceCallbackDO callback);
}
