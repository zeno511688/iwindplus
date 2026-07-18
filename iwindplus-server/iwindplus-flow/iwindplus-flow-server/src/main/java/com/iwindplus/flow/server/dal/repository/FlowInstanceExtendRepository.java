/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.flow.server.dal.mapper.FlowInstanceExtendMapper;
import com.iwindplus.flow.server.dal.model.FlowInstanceExtendDO;
import org.springframework.stereotype.Repository;

/**
 * 流程实例扩展聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowInstanceExtendRepository extends JoinCrudRepository<FlowInstanceExtendMapper, FlowInstanceExtendDO> {

    /**
     * 根据流程实例ID获取流程实例扩展信息.
     *
     * @param instanceId 流程实例ID
     * @return 流程实例扩展信息
     */
    public FlowInstanceExtendDO getByInstanceId(Long instanceId) {
        return this.getOne(
            Wrappers.lambdaQuery(FlowInstanceExtendDO.class)
                .eq(FlowInstanceExtendDO::getInstanceId, instanceId)
        );
    }
}
