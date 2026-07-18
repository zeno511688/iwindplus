/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.flow.domain.dto.FlowHisInstanceSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisInstancePageVO;

/**
 * 历史流程实例业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowHisInstanceService {

    /**
     * 我的发起分页查询.
     *
     * @param entity 查询条件
     * @return IPage<FlowHisInstancePageVO>
     */
    IPage<FlowHisInstancePageVO> myInitiatedPage(FlowHisInstanceSearchDTO entity);

    /**
     * 我的已办分页查询.
     *
     * @param entity 搜索条件
     * @return IPage<FlowHisInstancePageVO>
     */
    IPage<FlowHisInstancePageVO> myDonePage(FlowHisInstanceSearchDTO entity);

    /**
     * 抄送我的分页查询.
     *
     * @param entity 搜索条件
     * @return IPage<FlowHisInstancePageVO>
     */
    IPage<FlowHisInstancePageVO> myCcPage(FlowHisInstanceSearchDTO entity);

    /**
     * 所有分页查询.
     *
     * @param entity 搜索条件
     * @return IPage<FlowHisInstancePageVO>
     */
    IPage<FlowHisInstancePageVO> allPage(FlowHisInstanceSearchDTO entity);
}
