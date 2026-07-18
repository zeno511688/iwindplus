/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.flow.server.dal.mapper.FlowHisTaskMapper;
import com.iwindplus.flow.server.dal.model.FlowHisTaskDO;
import org.springframework.stereotype.Repository;

/**
 * 历史流程任务聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowHisTaskRepository extends JoinCrudRepository<FlowHisTaskMapper, FlowHisTaskDO> {

}
