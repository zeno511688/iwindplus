/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.asynctask.i18n;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigType;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskExecuteResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.support.AsyncTaskHandler;
import com.iwindplus.base.i18n.domain.constant.I18nConstant;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 国际化消息推送数据至Nacos异步执行器.
 *
 * @author zengdegui
 * @since 2025/12/29 00:42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class I18nMsgPushTaskHandler implements AsyncTaskHandler {

    private final Optional<NacosConfigManager> nacosConfigManagerOpt;

    @Override
    public AsyncTaskExecuteResultVO execute(AsyncTaskVO entity) {
        final Map<String, Object> paramMap = entity.getParam();
        final String fileName = paramMap.get("fileName").toString();
        final String content = paramMap.get("content").toString();

        if (nacosConfigManagerOpt.isEmpty()) {
            log.warn("NacosConfigManager not present, skip route push");
            return AsyncTaskExecuteResultVO.success();
        }
        NacosConfigManager nacosConfigManager = nacosConfigManagerOpt.get();

        try {
            boolean ok = nacosConfigManager.getConfigService().publishConfig(
                fileName, I18nConstant.I18N_GROUP, content, ConfigType.PROPERTIES.getType());
            if (!ok) {
                log.info("推送国际化文件={}，到 Nacos 失败", fileName);
            } else {
                log.info("推送国际化文件={}，到 Nacos 成功", fileName);
            }
        } catch (Exception ex) {
            log.error("推送国际化文件文件={}，到 Nacos Exception={}", fileName, ex);
        }
        return AsyncTaskExecuteResultVO.success();
    }
}
