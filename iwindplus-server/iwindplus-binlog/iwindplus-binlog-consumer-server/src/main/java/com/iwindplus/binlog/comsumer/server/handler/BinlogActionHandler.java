/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.factory.AlertExecutorStrategyFactory;
import com.iwindplus.base.domain.dto.ValidListDTO;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionCheckSignDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionProcessDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataProcessDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.SourceMetaDTO;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty.Webhook;
import com.iwindplus.binlog.comsumer.server.factory.BinlogActionStrategyFactory;
import com.iwindplus.log.client.BinlogAlertClient;
import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
     * @param entities binlog数据
     */
    public void processHandler(List<BinlogRowDataDTO> entities) {
        List<BinlogRowDataProcessDTO> list = new ArrayList<>(10);
        for (BinlogRowDataDTO entity : entities) {
            // 验签
            final DbActionTypeEnum actionType = DbActionTypeEnum.fromAlias(entity.getOp());
            BinlogActionProcessDTO<BinlogRowDataDTO> processDTO = BinlogActionProcessDTO
                .<BinlogRowDataDTO>builder()
                .actionType(actionType)
                .data(entity)
                .build();
            final BinlogActionCheckSignDTO checkSignDTO = this.binlogActionStrategyFactory.execute(processDTO);
            if (Objects.nonNull(checkSignDTO) && Boolean.TRUE.equals(checkSignDTO.getSuccess())) {
                continue;
            }

            // todo 处置
            buildParam(list, entity, actionType, checkSignDTO.getMessage());
        }

        if (CollUtil.isEmpty(list)) {
            return;
        }

        final List<BinlogAlertDTO> dtoList = list.stream()
            .map(BinlogRowDataProcessDTO::getBinlogAlert).toList();
        final ValidListDTO<BinlogAlertDTO> paramList = new ValidListDTO<>(dtoList);

        this.binlogTaskExecutor.execute(() -> {
            this.binlogAlertClient.saveBatch(paramList);

            for (BinlogRowDataProcessDTO entity : list) {
                final BinlogAlertDTO binlogAlert = entity.getBinlogAlert();
                this.sendMsg(entity.getSourceData(), binlogAlert.getActionType(), binlogAlert.getMessage());
            }
        });
    }

    private void buildParam(
        List<BinlogRowDataProcessDTO> list,
        BinlogRowDataDTO entity,
        DbActionTypeEnum actionType,
        String message) {

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
            .message(message)
            .build();

        final BinlogRowDataProcessDTO binlogRowDataProcess = BinlogRowDataProcessDTO.builder()
            .sourceData(entity)
            .binlogAlert(binlogAlertDTO)
            .build();
        list.add(binlogRowDataProcess);
    }

    private void sendMsg(BinlogRowDataDTO dto, DbActionTypeEnum actionType, String message) {
        final Webhook webhook = property.getWebhook();
        if (Objects.isNull(webhook) || Objects.isNull(webhook.getUrl())) {
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
