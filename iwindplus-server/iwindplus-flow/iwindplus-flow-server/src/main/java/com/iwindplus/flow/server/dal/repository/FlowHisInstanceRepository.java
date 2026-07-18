/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.flow.server.dal.mapper.FlowHisInstanceMapper;
import com.iwindplus.flow.server.dal.model.FlowHisInstanceDO;
import org.springframework.stereotype.Repository;

/**
 * 历史流程实例聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowHisInstanceRepository extends JoinCrudRepository<FlowHisInstanceMapper, FlowHisInstanceDO> {

}
