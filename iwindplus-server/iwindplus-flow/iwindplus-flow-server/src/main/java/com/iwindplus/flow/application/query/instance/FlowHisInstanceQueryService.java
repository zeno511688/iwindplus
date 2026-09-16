/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.query.instance;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.flow.application.query.instance.dto.FlowHisInstanceSearchDTO;
import com.iwindplus.flow.application.query.instance.vo.FlowHisInstancePageVO;
import com.iwindplus.flow.common.enums.FlowInstanceQueryTypeEnum;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowHisInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程实例查询业务层.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisInstanceQueryService {

    private final FlowHisInstanceRepository flowHisInstanceRepository;
    /**
     * 我的发起分页查询.
     *
     * @param entity 查询条件
     * @return IPage<FlowHisInstancePageVO>
     */
    public IPage<FlowHisInstancePageVO> myInitiatedPage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.MY_INITIATED);
        return getPage(entity);
    }

    /**
     * 我的已办分页查询.
     *
     * @param entity 搜索条件
     * @return IPage<FlowHisInstancePageVO>
     */
    public IPage<FlowHisInstancePageVO> myDonePage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.MY_DONE);
        return getPage(entity);
    }

    /**
     * 抄送我的分页查询.
     *
     * @param entity 搜索条件
     * @return IPage<FlowHisInstancePageVO>
     */
    public IPage<FlowHisInstancePageVO> myCcPage(FlowHisInstanceSearchDTO entity) {
        entity.setQueryType(FlowInstanceQueryTypeEnum.MY_CC);
        return getPage(entity);
    }

    /**
     * 所有分页查询.
     *
     * @param entity 搜索条件
     * @return IPage<FlowHisInstancePageVO>
     */
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
