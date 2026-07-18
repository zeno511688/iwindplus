/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.i18n.domain.constant.I18nConstant;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 国际化消息删除Nacos数据异步执行器.
 *
 * @author zengdegui
 * @since 2025/12/29 00:42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class I18nMsgRemoveTaskHandler implements AsyncCmdTaskHandler {

    private final Optional<NacosConfigManager> nacosConfigManagerOpt;

    @Override
    public void execute(AsyncCmdExecutorBO entity) {
        final Map<String, Object> contentMap = entity.getContent();
        final String fileName = contentMap.get("fileName").toString();

        if (nacosConfigManagerOpt.isEmpty()) {
            log.warn("NacosConfigManager not present, skip route push");
            return;
        }
        NacosConfigManager nacosConfigManager = nacosConfigManagerOpt.get();

        String dataId = fileName.endsWith(I18nConstant.FILE_SUFFIX) ? fileName : fileName + I18nConstant.FILE_SUFFIX;
        try {
            boolean ok = nacosConfigManager.getConfigService().removeConfig(dataId, I18nConstant.I18N_GROUP);
            if (!ok) {
                log.info("删除 Nacos 国际化文件={}，失败", dataId);
            } else {
                log.info("删除 Nacos 国际化文件={}，成功", dataId);
            }
        } catch (Exception ex) {
            log.error("删除 Nacos 国际化文件={}， Exception={}", dataId, ex);
        }
    }
}
