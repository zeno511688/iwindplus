/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.task;

import com.iwindplus.flow.infrastructure.persistence.task.FlowHisTaskPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程任务参与人业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisTaskPlayerApplicationService {

    private final FlowHisTaskPlayerRepository flowHisTaskPlayerRepository;

}
