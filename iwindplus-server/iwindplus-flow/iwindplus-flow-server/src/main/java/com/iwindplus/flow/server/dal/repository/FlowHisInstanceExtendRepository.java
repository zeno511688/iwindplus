/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.flow.server.dal.mapper.FlowHisInstanceExtendMapper;
import com.iwindplus.flow.server.dal.model.FlowHisInstanceExtendDO;
import org.springframework.stereotype.Repository;

/**
 * 历史流程实例扩展聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowHisInstanceExtendRepository extends JoinCrudRepository<FlowHisInstanceExtendMapper, FlowHisInstanceExtendDO> {

}
