/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.server.dal.mapper.FlowTaskMapper;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import org.springframework.stereotype.Repository;

/**
 * 流程任务聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowTaskRepository extends JoinCrudRepository<FlowTaskMapper, FlowTaskDO> {

    /**
     * 锁定任务（使用数据库行锁）.
     *
     * @param taskId 任务ID
     * @return 任务对象
     * @throws BizException 任务不存在时抛出异常
     */
    public FlowTaskDO getTaskForLock(Long taskId) {
        FlowTaskDO task = this.getOne(
            Wrappers.lambdaQuery(FlowTaskDO.class)
                .eq(FlowTaskDO::getId, taskId)
                .last("FOR UPDATE")
        );

        if (task == null) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return task;
    }
}
