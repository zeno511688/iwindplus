/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.support.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.dtx.domain.dto.TccGlobalTxSearchDTO;
import com.iwindplus.dtx.domain.vo.TccGlobalTxPageVO;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import com.iwindplus.dtx.server.config.property.DtxProperty;
import com.iwindplus.dtx.server.coordinator.TccCoordinator;
import com.iwindplus.dtx.server.service.TccBranchTxService;
import com.iwindplus.dtx.server.support.DtxJobHandler;
import com.iwindplus.dtx.server.service.TccGlobalTxService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
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
public abstract class AbstractDtxJobHandlerImpl implements DtxJobHandler {

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
     */
    protected abstract void doExecute(List<TccGlobalTxVO> entityList);

    /**
     * 获取查询参数.
     *
     * @return TccGlobalTxSearchDTO
     */
    protected abstract TccGlobalTxSearchDTO buildDtxJobSearchDTO();

    @Override
    public void execute(int shardingIndex) {
        final TccGlobalTxSearchDTO param = this.buildDtxJobSearchDTO();
        param.setEnv(SpringUtil.getActiveProfile());
        param.setCurrent(shardingIndex + 1);
        param.setSize(tccCoordinator.getSize());
        List<TccGlobalTxVO> list = this.list(param);
        log.info("DtxJobManager 查询结果总数={}", list.size());
        if (CollUtil.isEmpty(list)) {
            return;
        }

        this.doExecute(list);
    }

    private List<TccGlobalTxVO> list(TccGlobalTxSearchDTO param) {
        log.info("执行job, 当前页={}, 每页条数={}", param.getCurrent(), param.getSize());

        List<TccGlobalTxVO> list = new ArrayList<>(10);
        IPage<TccGlobalTxPageVO> page = this.globalTxService.page(param);
        if (Objects.nonNull(page) && CollUtil.isNotEmpty(page.getRecords())) {
            list.addAll(BeanUtil.copyToList(page.getRecords(), TccGlobalTxVO.class));
        }
        return list;
    }
}
