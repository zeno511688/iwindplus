/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigType;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
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
public class I18nMsgPushTaskHandler implements AsyncCmdTaskHandler {

    private final Optional<NacosConfigManager> nacosConfigManagerOpt;

    @Override
    public void execute(AsyncCmdExecutorBO entity) {
        final Map<String, Object> contentMap = entity.getContent();
        final String fileName = contentMap.get("fileName").toString();
        final String content = contentMap.get("content").toString();

        if (nacosConfigManagerOpt.isEmpty()) {
            log.warn("NacosConfigManager not present, skip route push");
            return;
        }
        NacosConfigManager nacosConfigManager = nacosConfigManagerOpt.get();

        String dataId = fileName;
        try {
            boolean ok = nacosConfigManager.getConfigService().publishConfig(
                dataId, I18nConstant.I18N_GROUP, content, ConfigType.PROPERTIES.getType());
            if (!ok) {
                log.info("推送国际化文件={}，到 Nacos 失败", dataId);
            } else {
                log.info("推送国际化文件={}，到 Nacos 成功", dataId);
            }
        } catch (Exception ex) {
            log.error("推送国际化文件文件={}，到 Nacos Exception={}", dataId, ex);
        }
    }
}
