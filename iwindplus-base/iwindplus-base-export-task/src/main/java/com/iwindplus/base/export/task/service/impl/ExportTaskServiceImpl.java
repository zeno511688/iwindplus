/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.export.task.dal.model.ExportTaskDO;
import com.iwindplus.base.export.task.dal.repository.ExportTaskRepository;
import com.iwindplus.base.export.task.domain.dto.ExportTaskDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskShardSearchDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.service.ExportTaskService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 导出任务Service实现类.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Slf4j
@RequiredArgsConstructor
public class ExportTaskServiceImpl implements ExportTaskService {

    private final ExportTaskProperty property;
    private final ExportTaskRepository exportTaskRepository;
    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Integer getSize() {
        int activeCount = threadPoolExecutor.getActiveCount();
        int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
        final int queueSize = threadPoolExecutor.getQueue().size();
        int available = maxPoolSize - activeCount - queueSize;
        return Math.max(0, Math.min(this.property.getMaxPageSize(), available));
    }

    @Override
    public ExportTaskVO save(ExportTaskDTO entity) {
        return this.exportTaskRepository.save(entity);
    }

    @Override
    public boolean editStatusById(ExportTaskStatusEditDTO entity) {
        return this.exportTaskRepository.updateStatusById(entity);
    }

    @Override
    public List<ExportTaskVO> listByShard(ExportTaskShardSearchDTO param) {
        LambdaQueryWrapper<ExportTaskDO> queryWrapper = Wrappers.lambdaQuery(ExportTaskDO.class)
            .gt(ExportTaskDO::getId, Objects.isNull(param.getLastId()) ? 0L : param.getLastId())
            .orderByAsc(ExportTaskDO::getId)
            .last("LIMIT " + param.getSize());

        final Integer shardTotal = param.getShardTotal();
        if (Objects.nonNull(shardTotal) && shardTotal > 1) {
            final int shardIndex = Objects.isNull(param.getShardIndex()) ? 0 : param.getShardIndex();
            // 使用范围分片，可以利用主键索引提升性能
            // 每个分片处理 ID 范围为 [minId, maxId) 的数据
            long minId = Objects.isNull(param.getLastId()) ? 0L : param.getLastId();
            long shardMinId = minId + shardIndex * param.getSize();
            long shardMaxId = shardMinId + param.getSize();
            queryWrapper.ge(ExportTaskDO::getId, shardMinId)
                .lt(ExportTaskDO::getId, shardMaxId);
        }
        // 状态条件
        if (CollUtil.isNotEmpty(param.getStatusList())) {
            queryWrapper.in(ExportTaskDO::getStatus, param.getStatusList());
        }

        List<ExportTaskDO> list = this.exportTaskRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream()
            .map(entity -> BeanUtil.copyProperties(entity, ExportTaskVO.class))
            .toList();
    }

    @Override
    public ExportTaskVO getDetail(Long id) {
        ExportTaskDO entity = this.exportTaskRepository.getById(id);
        if (Objects.isNull(entity)) {
            return null;
        }
        return BeanUtil.copyProperties(entity, ExportTaskVO.class);
    }

    @Override
    public ExportTaskVO getDetailByBizNumber(String bizNumber) {
        final ExportTaskDO data = this.exportTaskRepository.getOne(Wrappers.lambdaQuery(ExportTaskDO.class)
            .eq(ExportTaskDO::getBizNumber, bizNumber)
            .orderByDesc(ExportTaskDO::getId)
            .last("LIMIT 1"));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ExportTaskVO.class);
    }
}
