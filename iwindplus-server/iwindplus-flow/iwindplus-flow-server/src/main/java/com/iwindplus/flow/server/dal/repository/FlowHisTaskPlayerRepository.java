/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.flow.server.dal.mapper.FlowHisTaskPlayerMapper;
import com.iwindplus.flow.server.dal.model.FlowHisTaskPlayerDO;
import org.springframework.stereotype.Repository;

/**
 * 历史流程任务参与人聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowHisTaskPlayerRepository extends JoinCrudRepository<FlowHisTaskPlayerMapper, FlowHisTaskPlayerDO> {

}
