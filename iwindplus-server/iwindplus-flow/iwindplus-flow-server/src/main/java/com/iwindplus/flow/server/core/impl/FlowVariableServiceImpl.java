/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.flow.server.core.FlowVariableService;
import com.iwindplus.flow.server.dal.model.FlowInstanceExtendDO;
import com.iwindplus.flow.server.dal.repository.FlowInstanceExtendRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程变量业务层接口实现类.
 * <p>
 * 负责流程实例变量的加载、合并等操作。
 * 变量以JSON格式存储在实例扩展表中。
 *
 * @author zengdegui
 * @since 2026/05/22 23:43
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowVariableServiceImpl implements FlowVariableService {

    private final FlowInstanceExtendRepository flowInstanceExtendRepository;

    @Override
    public Map<String, Object> loadVariables(Long instanceId) {
        if (instanceId == null) {
            return new HashMap<>(16);
        }

        FlowInstanceExtendDO extend = flowInstanceExtendRepository.getOne(
            Wrappers.lambdaQuery(FlowInstanceExtendDO.class)
                .eq(FlowInstanceExtendDO::getInstanceId, instanceId)
        );

        if (extend == null || CharSequenceUtil.isBlank(extend.getVariable())) {
            return new HashMap<>(16);
        }

        // 解析JSON变量
        return JacksonUtil.parseObject(extend.getVariable(), Map.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeVariables(Long instanceId, Map<String, Object> variables) {
        if (instanceId == null || CollUtil.isEmpty(variables)) {
            return;
        }

        // 查询实例扩展记录
        FlowInstanceExtendDO ext = flowInstanceExtendRepository.getOne(
            Wrappers.lambdaQuery(FlowInstanceExtendDO.class)
                .eq(FlowInstanceExtendDO::getInstanceId, instanceId)
        );

        if (ext == null) {
            return;
        }

        // 解析现有变量
        Map<String, Object> oldVars = CharSequenceUtil.isNotBlank(ext.getVariable())
            ? JacksonUtil.parseObject(ext.getVariable(), Map.class)
            : new HashMap<>(16);

        // 合并新变量（新值覆盖旧值）
        oldVars.putAll(variables);

        // 更新扩展表
        ext.setVariable(JacksonUtil.toJsonStr(oldVars));
        flowInstanceExtendRepository.updateById(ext);
    }
}
