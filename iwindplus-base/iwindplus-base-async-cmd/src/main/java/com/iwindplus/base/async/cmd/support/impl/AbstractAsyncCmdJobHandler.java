/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdShardSearchDTO;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdJobHandler;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象异步命令job助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractAsyncCmdJobHandler implements AsyncCmdJobHandler {

    private final AsyncCmdProperty property;
    private final AsyncCmdService asyncCmdService;

    /**
     * 执行.
     *
     * @param entityList 集合
     * @return boolean
     */
    protected abstract boolean doExecute(List<AsyncCmdVO> entityList);

    /**
     * 获取查询参数.
     *
     * @return AsyncCmdShardSearchDTO
     */
    protected abstract AsyncCmdShardSearchDTO buildJobSearchDTO();

    /**
     * 判断是否应该跳过该任务.
     *
     * @param entity 任务实体
     * @return true=跳过，false=不跳过
     */
    protected boolean shouldSkip(AsyncCmdVO entity) {
        return false;
    }

    @Override
    public void execute(Integer shardIndex, Integer shardTotal) {
        final Integer size = this.asyncCmdService.getSize();
        if (Objects.isNull(size) || size <= 0) {
            log.error("【{}】每轮捞取条数={}, 本轮不捞取，请检查每页条数配置", this.support(), size);
            return;
        }

        final AbstractAsyncCmdJobHandler proxy = SpringUtil.getBean(this.getClass());

        final AsyncCmdShardSearchDTO param = this.buildJobSearchDTO();
        param.setShardIndex(shardIndex);
        param.setShardTotal(shardTotal);
        param.setSize(size);

        long lastId = 0;
        int loop = 0;
        int total = 0;
        while (loop < NumberConstant.NUMBER_ONE_HUNDRED) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            param.setLastId(lastId);

            final List<AsyncCmdVO> list = this.asyncCmdService.listByShard(param);
            if (CollUtil.isEmpty(list)) {
                break;
            }

            // 更新游标
            lastId = list.get(list.size() - 1).getId();
            loop++;
            total += list.size();

            if (!proxy.doExecute(list)) {
                log.warn("【{}】下游已经包含，提前结束本躺捞取，已处理={}", this.support(), total);

                break;
            }
        }

        log.info("【{}】执行完成，分片={}/{} 轮次={}, 共处理【{}】条数据",
            this.support(), shardIndex, shardTotal,
            loop, total);
    }

}
