/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.document.domain.dto.ExportTaskShardSearchDTO;
import com.iwindplus.base.document.domain.property.DocumentProperty;
import com.iwindplus.base.document.domain.vo.ExportTaskVO;
import com.iwindplus.base.document.service.ExportTaskService;
import com.iwindplus.base.document.support.DocumentTaskJobHandler;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象导出任务job助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractExportTaskJobHandler implements DocumentTaskJobHandler {

    private final DocumentProperty property;
    private final ExportTaskService exportTaskService;

    /**
     * 执行.
     *
     * @param entityList 集合
     * @return boolean
     */
    protected abstract boolean doExecute(List<ExportTaskVO> entityList);

    /**
     * 获取查询参数.
     *
     * @return ExportTaskShardSearchDTO
     */
    protected abstract ExportTaskShardSearchDTO buildJobSearchDTO();

    /**
     * 判断是否应该跳过该任务.
     *
     * @param entity 任务实体
     * @return true=跳过，false=不跳过
     */
    protected boolean shouldSkip(ExportTaskVO entity) {
        return false;
    }

    @Override
    public void execute(Integer shardIndex, Integer shardTotal) {
        final Integer size = this.exportTaskService.getSize();
        if (Objects.isNull(size) || size <= 0) {
            log.error("【{}】导出任务每轮捞取条数={}, 本轮不捞取，请检查每页条数配置", this.support(), size);
            return;
        }

        final AbstractExportTaskJobHandler proxy = SpringUtil.getBean(this.getClass());

        final ExportTaskShardSearchDTO param = this.buildJobSearchDTO();
        param.setShardIndex(shardIndex);
        param.setShardTotal(shardTotal);
        param.setSize(size);

        long lastId = 0;
        int loop = 0;
        int total = 0;
        while (loop < this.property.getJob().getMaxLoopCount()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            param.setLastId(lastId);

            final List<ExportTaskVO> list = this.exportTaskService.listByShard(param);
            if (CollUtil.isEmpty(list)) {
                break;
            }

            final boolean result = proxy.doExecute(list);
            if (!result) {
                log.warn("【{}】导出任务执行失败，提前结束本轮捞取，已处理={}", this.support(), total);

                break;
            }

            // 更新游标
            lastId = list.get(list.size() - 1).getId();
            loop++;
            total += list.size();
        }
    }

}
