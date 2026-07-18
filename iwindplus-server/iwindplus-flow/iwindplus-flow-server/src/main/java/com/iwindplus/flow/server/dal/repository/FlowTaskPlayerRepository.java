/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.repository;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.server.dal.mapper.FlowTaskPlayerMapper;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 流程任务参与人聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowTaskPlayerRepository extends JoinCrudRepository<FlowTaskPlayerMapper, FlowTaskPlayerDO> {

    /**
     * 获取指定任务的最大序号.
     *
     * @param taskId 任务ID
     * @return 最大序号，如果没有记录则返回0
     */
    public Integer getMaxSeqByTaskId(Long taskId) {
        FlowTaskPlayerDO player = this.getOne(
            Wrappers.lambdaQuery(FlowTaskPlayerDO.class)
                .eq(FlowTaskPlayerDO::getTaskId, taskId)
                .select(FlowTaskPlayerDO::getSeq)
                .orderByDesc(FlowTaskPlayerDO::getSeq)
                .last("LIMIT 1")
        );
        return Optional.ofNullable(player).map(FlowTaskPlayerDO::getSeq).orElse(0);
    }

    /**
     * 获取指定任务的所有参与人.
     *
     * @param taskId 任务ID
     * @return 参与人列表
     * @throws BizException 任务不存在时抛出异常
     */
    public List<FlowTaskPlayerDO> listTaskPlayers(Long taskId) {
        List<FlowTaskPlayerDO> players =
            this.list(
                Wrappers.lambdaQuery(FlowTaskPlayerDO.class)
                    .eq(FlowTaskPlayerDO::getTaskId, taskId)
            );

        if (CollUtil.isEmpty(players)) {
            throw new BizException(FlowCodeEnum.FLOW_TASK_NOT_PLAYER);
        }

        return players;
    }
}
