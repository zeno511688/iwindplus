/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.flow.domain.dto.FlowTaskEditDTO;
import com.iwindplus.flow.domain.dto.FlowTaskSaveDTO;
import com.iwindplus.flow.domain.dto.FlowTaskSearchDTO;
import com.iwindplus.flow.domain.vo.FlowTaskPageVO;
import java.util.List;

/**
 * 流程任务业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowTaskService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowTaskSaveDTO entity);

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
    boolean edit(FlowTaskEditDTO entity);

    /**
     * 我的待办分页查询.
     *
     * @param entity 查询条件
     * @return IPage<FlowTaskPageVO>
     */
    IPage<FlowTaskPageVO> myPendingPage(FlowTaskSearchDTO entity);
}
