/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.query.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.flow.application.query.task.dto.FlowTaskSearchDTO;
import com.iwindplus.flow.application.query.task.vo.FlowTaskPageVO;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 流程任务业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@RequiredArgsConstructor
public class FlowTaskQueryService {

    private final FlowTaskRepository flowTaskRepository;

    /**
     * 我的待办分页查询.
     *
     * @param entity 查询条件
     * @return IPage<FlowTaskPageVO>
     */
    public IPage<FlowTaskPageVO> myPendingPage(FlowTaskSearchDTO entity) {
        PageDTO<FlowTaskPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowTaskRepository.getBaseMapper().selectMyPendingPage(page, entity);
    }
}
