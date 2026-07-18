/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.factory.AlertExecutorStrategyFactory;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionCheckSignDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionProcessDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.SourceMetaDTO;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty.Webhook;
import com.iwindplus.binlog.comsumer.server.factory.BinlogActionStrategyFactory;
import com.iwindplus.log.client.BinlogAlertClient;
import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Component;

/**
 * binlog 处理助手.
 *
 * @author zengdegui
 * @since 2025/11/30 00:23
 */
@Slf4j
@Component
public class BinlogActionHandler {

    @Resource
    protected BinLogConsumerProperty property;

    @Resource
    private BinlogActionStrategyFactory<BinlogRowDataDTO, BinlogActionCheckSignDTO> binlogActionStrategyFactory;

    @Resource
    private AlertExecutorStrategyFactory alertExecutorStrategyFactory;

    @Resource
    private BinlogAlertClient binlogAlertClient;

    @Resource
    private DtpExecutor binlogTaskExecutor;

    /**
     * 处理binlog数据.
     *
     * @param entity binlog数据
     */
    public void processHandler(BinlogRowDataDTO entity) {
        // 验签
        final DbActionTypeEnum actionType = DbActionTypeEnum.fromAlias(entity.getOp());
        BinlogActionProcessDTO<BinlogRowDataDTO> processDTO = BinlogActionProcessDTO
            .<BinlogRowDataDTO>builder()
            .actionType(actionType)
            .data(entity)
            .build();
        final BinlogActionCheckSignDTO checkSignDTO = this.binlogActionStrategyFactory.execute(processDTO);
        if (Objects.nonNull(checkSignDTO) && Boolean.TRUE.equals(checkSignDTO.getSuccess())) {
            return;
        }

        // todo 处置

        final SourceMetaDTO source = entity.getSource();
        final BinlogAlertDTO binlogAlertDTO = BinlogAlertDTO.builder()
            .tsMs(entity.getTsMs())
            .db(source.getDb())
            .table(source.getTable())
            .dataId(entity.getDataId())
            .file(source.getFile())
            .pos(source.getPos())
            .actionType(actionType)
            .before(JacksonUtil.toJsonStr(entity.getBefore()))
            .after(JacksonUtil.toJsonStr(entity.getAfter()))
            .message(checkSignDTO.getMessage())
            .build();
        this.binlogTaskExecutor.execute(() -> {
            this.binlogAlertClient.save(binlogAlertDTO);
            this.sendMsg(entity, actionType, checkSignDTO.getMessage());
        });
    }

    private void sendMsg(BinlogRowDataDTO dto, DbActionTypeEnum actionType, String message) {
        final Webhook webhook = property.getWebhook();
        if (Objects.isNull(webhook)) {
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
            .webhookUrl(webhook.getUrl())
            .secret(webhook.getSecret())
            .content(data)
            .build();
        this.alertExecutorStrategyFactory
            .getDefaultAlertExecutor()
            .sendWebhookMsg(entity);
    }

}
