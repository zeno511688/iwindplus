/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.flow.domain.dto.FlowHisInstanceSearchDTO;
import com.iwindplus.flow.domain.enums.FlowInstanceQueryTypeEnum;
import com.iwindplus.flow.domain.vo.FlowHisInstancePageVO;
import com.iwindplus.flow.server.dal.repository.FlowHisInstanceRepository;
import com.iwindplus.flow.server.service.FlowHisInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程实例业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisInstanceServiceImpl implements FlowHisInstanceService {

    private final FlowHisInstanceRepository flowHisInstanceRepository;

    @Override
    public IPage<FlowHisInstancePageVO> myInitiatedPage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.MY_INITIATED);
        return getPage(entity);
    }

    @Override
    public IPage<FlowHisInstancePageVO> myDonePage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.MY_DONE);
        return getPage(entity);
    }

    @Override
    public IPage<FlowHisInstancePageVO> myCcPage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.MY_CC);
        return getPage(entity);
    }

    @Override
    public IPage<FlowHisInstancePageVO> allPage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.ALL);
        return getPage(entity);
    }

    private IPage<FlowHisInstancePageVO> getPage(FlowHisInstanceSearchDTO entity) {
        PageDTO<FlowHisInstancePageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowHisInstanceRepository.getBaseMapper().selectPage(page, entity);
    }
}
