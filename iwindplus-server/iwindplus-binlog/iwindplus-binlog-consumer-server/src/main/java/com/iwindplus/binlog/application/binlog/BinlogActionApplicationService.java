/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.application.binlog;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.dto.ValidListDTO;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.binlog.application.binlog.dto.BinlogRowDataProcessDTO;
import com.iwindplus.binlog.domain.binlog.constant.BinlogConsumerConstant;
import com.iwindplus.binlog.domain.binlog.model.BinlogActionCheckSignDTO;
import com.iwindplus.binlog.domain.binlog.model.BinlogActionProcessDTO;
import com.iwindplus.binlog.domain.binlog.model.BinlogRowDataDTO;
import com.iwindplus.binlog.domain.binlog.model.SourceMetaDTO;
import com.iwindplus.binlog.domain.binlog.service.BinlogAlertNotifier;
import com.iwindplus.binlog.domain.binlog.strategy.BinlogActionHandlerFactory;
import com.iwindplus.log.api.dto.BinlogAlertDTO;
import com.iwindplus.log.client.BinlogAlertClient;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Component;

/**
 * binlog处理业务层.
 *
 * @author zengdegui
 * @since 2025/11/30 00:23
 */
@Slf4j
@Component
public class BinlogActionApplicationService {

    @Resource
    private BinlogActionHandlerFactory<BinlogRowDataDTO, BinlogActionCheckSignDTO> binlogActionFactory;

    @Resource
    private BinlogAlertNotifier binlogAlertNotifier;

    @Resource
    private BinlogAlertClient binlogAlertClient;

    @Resource(name = BinlogConsumerConstant.THREAD_POOL_BEAN_NAME)
    private DtpExecutor threadPoolExecutor;

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
            final BinlogActionCheckSignDTO checkSignDTO = this.binlogActionFactory.execute(processDTO);
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

        this.threadPoolExecutor.execute(() -> {
            try {
                this.binlogAlertClient.saveBatch(paramList);
            } catch (Exception e) {
                log.error("binlogAlertClient.saveBatch error: {}", e.getMessage());
            }

            for (BinlogRowDataProcessDTO entity : list) {
                final BinlogAlertDTO binlogAlert = entity.getBinlogAlert();
                this.binlogAlertNotifier.sendMsg(entity.getSourceData(), binlogAlert.getActionType(), binlogAlert.getMessage());
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

}
