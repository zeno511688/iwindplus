/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.flow.server.dal.mapper.FlowInstanceCallbackMapper;
import com.iwindplus.flow.server.dal.model.FlowInstanceCallbackDO;
import org.springframework.stereotype.Repository;

/**
 * 流程实例回调聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowInstanceCallbackRepository extends JoinCrudRepository<FlowInstanceCallbackMapper, FlowInstanceCallbackDO> {

    /**
     * 根据实例ID查询回调记录.
     *
     * @param instanceId 实例ID
     * @return 回调记录
     */
    public FlowInstanceCallbackDO getByInstanceId(Long instanceId) {
        return this.getOne(
            Wrappers.lambdaQuery(FlowInstanceCallbackDO.class)
                .eq(FlowInstanceCallbackDO::getInstanceId, instanceId)
        );
    }
}
