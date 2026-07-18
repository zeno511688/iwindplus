/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSearchDTO;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdPageVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdJobHandler;
import java.util.ArrayList;
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
     */
    protected abstract void doExecute(List<AsyncCmdBO> entityList);

    /**
     * 获取查询参数.
     *
     * @return AsyncCmdSearchDTO
     */
    protected abstract AsyncCmdSearchDTO buildJobSearchDTO();

    @Override
    public void execute(int shardingIndex) {
        final AsyncCmdSearchDTO param = this.buildJobSearchDTO();
        param.setEnv(SpringUtil.getActiveProfile());
        param.setCurrent(shardingIndex + 1);
        param.setSize(asyncCmdService.getSize());
        List<AsyncCmdBO> list = this.list(param);

        log.info("任务={} 当前页={} 每页条数={} 结果总数={}",
            param.getTaskName(), param.getCurrent(),
            param.getSize(), list.size());

        if (CollUtil.isEmpty(list)) {
            return;
        }

        final AbstractAsyncCmdJobHandler proxy = SpringUtil.getBean(this.getClass());
        proxy.doExecute(list);
    }

    private List<AsyncCmdBO> list(AsyncCmdSearchDTO param) {
        List<AsyncCmdBO> list = new ArrayList<>(10);
        IPage<AsyncCmdPageVO> page = this.asyncCmdService.page(param);
        if (Objects.nonNull(page) && CollUtil.isNotEmpty(page.getRecords())) {
            list.addAll(BeanUtil.copyToList(page.getRecords(), AsyncCmdBO.class));
        }
        return list;
    }
}
