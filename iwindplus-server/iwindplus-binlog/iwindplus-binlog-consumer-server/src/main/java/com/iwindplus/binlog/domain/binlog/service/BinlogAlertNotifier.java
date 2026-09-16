/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.domain.binlog.service;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.alert.domain.constant.AlertConstant.FeishuConstant;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.factory.AlertExecuteHandlerFactory;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.binlog.domain.binlog.model.BinlogRowDataDTO;
import com.iwindplus.binlog.domain.binlog.model.SourceMetaDTO;
import com.iwindplus.binlog.infrastructure.configuration.BinLogConsumerProperty;
import com.iwindplus.binlog.infrastructure.configuration.BinLogConsumerProperty.AlertCfg;
import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * binlog 告警通知领域服务.
 *
 * @author zengdegui
 * @since 2026/09/15 15:30
 */
@Slf4j
@Component
public class BinlogAlertNotifier {

    @Resource
    private BinLogConsumerProperty property;

    @Resource
    private AlertExecuteHandlerFactory alertExecuteHandlerFactory;

    /**
     * 发送告警消息.
     *
     * @param dto         行数据
     * @param actionType  操作类型
     * @param message     告警原因
     */
    public void sendMsg(BinlogRowDataDTO dto, DbActionTypeEnum actionType, String message) {
        final AlertCfg channel = property.getAlert();
        if (Objects.isNull(channel) || Objects.isNull(channel.getCode()) || Objects.isNull(channel.getWebhookCode())) {
            return;
        }

        final SourceMetaDTO source = dto.getSource();

        final String data = new StringBuilder("数据库表数据被篡改").append("\n\n")
            .append("环境：").append(SpringUtil.getActiveProfile()).append("\n")
            .append("库名：").append(source.getDb()).append("\n")
            .append("表名：").append(source.getTable()).append("\n")
            .append("操作：").append(actionType.name()).append("\n")
            .append("操作前主键：").append(dto.getBeforeId()).append("\n")
            .append("操作前加签盐：").append(dto.getBeforeSalt()).append("\n")
            .append("操作后主键：").append(dto.getAfterId()).append("\n")
            .append("操作后加签盐：").append(dto.getAfterSalt()).append("\n")
            .append("原因：").append(message).append("\n").toString();

        final AlertWebhookRequestDTO entity = AlertWebhookRequestDTO
            .builder()
            .code(channel.getWebhookCode())
            .content(data)
            .build();

        this.alertExecuteHandlerFactory
            .getHandler(channel.getChannelType(),
                Optional.ofNullable(channel.getCode()).orElse(FeishuConstant.MAIN_CODE))
            .sendWebhookMsg(entity);
    }
}
