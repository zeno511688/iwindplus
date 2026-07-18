/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import com.iwindplus.flow.server.dal.repository.FlowHisTaskPlayerRepository;
import com.iwindplus.flow.server.service.FlowHisTaskPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程任务参与人业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisTaskPlayerServiceImpl implements FlowHisTaskPlayerService {

    private final FlowHisTaskPlayerRepository flowHisTaskPlayerRepository;

}
