/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.query.instance;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.flow.application.query.instance.dto.FlowInstanceCallbackShardSearchDTO;
import com.iwindplus.flow.application.query.instance.vo.FlowInstanceCallbackVO;
import com.iwindplus.flow.infrastructure.configuration.FlowProperty;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceCallbackDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceCallbackRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 流程实例回调查询业务层.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowInstanceCallbackQueryService {

    private final FlowInstanceCallbackRepository flowInstanceCallbackRepository;
    private final FlowProperty flowProperty;

    /**
     * 获取每页条数.
     *
     * @return Integer
     */
    public Integer getSize() {
        return this.flowProperty.getMaxPageSize();
    }

    /**
     * 分片查询.
     *
     * @param entity 搜索条件
     * @return List<FlowInstanceCallbackVO>
     */
    public List<FlowInstanceCallbackVO> listByShard(FlowInstanceCallbackShardSearchDTO entity) {
        LambdaQueryWrapper<FlowInstanceCallbackDO> queryWrapper = Wrappers.lambdaQuery(FlowInstanceCallbackDO.class)
            .gt(FlowInstanceCallbackDO::getId, Objects.isNull(entity.getLastId()) ? 0L : entity.getLastId())
            .orderByAsc(FlowInstanceCallbackDO::getId)
            .last("LIMIT " + entity.getSize());

        final Integer shardTotal = entity.getShardTotal();
        if (Objects.nonNull(shardTotal) && shardTotal > 1) {
            final int shardIndex = Objects.isNull(entity.getShardIndex()) ? 0 : entity.getShardIndex();
            queryWrapper.apply("MOD(id, {0}) = {1}", shardTotal, shardIndex);
        }
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(FlowInstanceCallbackDO::getStatus, entity.getStatus());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(FlowInstanceCallbackDO::getStatus, entity.getStatusList());
        }

        final List<FlowInstanceCallbackDO> result = this.flowInstanceCallbackRepository.list(queryWrapper);
        return BeanUtil.copyToList(result, FlowInstanceCallbackVO.class);
    }
}
