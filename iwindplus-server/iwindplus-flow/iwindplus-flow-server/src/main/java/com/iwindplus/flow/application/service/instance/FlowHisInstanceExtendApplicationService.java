/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.instance;

import com.iwindplus.flow.infrastructure.persistence.instance.FlowHisInstanceExtendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程实例扩展业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisInstanceExtendApplicationService {

    private final FlowHisInstanceExtendRepository flowHisInstanceExtendRepository;

}
