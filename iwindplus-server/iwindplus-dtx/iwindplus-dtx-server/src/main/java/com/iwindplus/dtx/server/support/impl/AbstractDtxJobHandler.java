/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.dtx.domain.dto.TccGlobalTxShardSearchDTO;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import com.iwindplus.dtx.server.config.property.DtxProperty;
import com.iwindplus.dtx.server.coordinator.TccCoordinator;
import com.iwindplus.dtx.server.service.TccBranchTxService;
import com.iwindplus.dtx.server.service.TccGlobalTxService;
import com.iwindplus.dtx.server.support.DtxJobHandler;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象分布式事务job操作策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public abstract class AbstractDtxJobHandler implements DtxJobHandler {

    @Resource
    protected DtxProperty property;

    @Resource
    protected TccGlobalTxService globalTxService;

    @Resource
    protected TccBranchTxService branchTxService;

    @Resource
    protected TccCoordinator tccCoordinator;

    /**
     * 执行.
     *
     * @param entityList 集合
     * @return boolean
     */
    protected abstract boolean doExecute(List<TccGlobalTxVO> entityList);

    /**
     * 获取查询参数.
     *
     * @return TccGlobalTxShardSearchDTO
     */
    protected abstract TccGlobalTxShardSearchDTO buildJobSearchDTO();

    @Override
    public void execute(Integer shardIndex, Integer shardTotal) {
        final Integer size = this.tccCoordinator.getSize();
        if (Objects.isNull(size) || size <= 0) {
            log.error("【{}】每轮捞取条数={}, 本轮不捞取，请检查每页条数配置", this.support(), size);
            return;
        }

        final AbstractDtxJobHandler proxy = SpringUtil.getBean(this.getClass());

        final TccGlobalTxShardSearchDTO param = this.buildJobSearchDTO();
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

            final List<TccGlobalTxVO> list = this.globalTxService.listByShard(param);
            if (CollUtil.isEmpty(list)) {
                break;
            }

            final boolean result = proxy.doExecute(list);
            if (!result) {
                log.warn("【{}】执行失败，提前结束本轮捞取，已处理={}", this.support(), total);

                break;
            }

            // 更新游标
            lastId = list.get(list.size() - 1).getId();
            loop++;
            total += list.size();
        }
    }
}
