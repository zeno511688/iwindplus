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
     */
    protected abstract void doExecute(List<AsyncCmdVO> entityList);

    /**
     * 获取查询参数.
     *
     * @return AsyncCmdShardSearchDTO
     */
    protected abstract AsyncCmdShardSearchDTO buildJobSearchDTO();

    @Override
    public void execute(Integer shardIndex, Integer shardTotal) {
        final AbstractAsyncCmdJobHandler proxy = SpringUtil.getBean(this.getClass());

        final AsyncCmdShardSearchDTO param = this.buildJobSearchDTO();
        param.setShardIndex(shardIndex);
        param.setShardTotal(shardTotal);

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

            proxy.doExecute(list);

            // 更新游标
            lastId = list.get(list.size() - 1).getId();
            loop++;
            total += list.size();
        }

        log.info("【{}】执行完成，分片={}/{} 轮次={}, 共处理【{}】条数据",
            this.getClass().getSimpleName(), shardIndex, shardTotal,
            loop, total);
    }

}
