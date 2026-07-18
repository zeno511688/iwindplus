/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.strategy.impl;

import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionCheckSignDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogActionProcessDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.domain.enums.BinlogConsumerCodeEnum;
import jakarta.annotation.Resource;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * 抽象binlog 操作策略实现类（删除）.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Component
public class BinlogActionDeleteStrategyImpl extends AbstractBinlogActionStrategyImpl<BinlogRowDataDTO, BinlogActionCheckSignDTO> {

    @Resource
    private MessageSource messageSource;

    @Override
    public DbActionTypeEnum support() {
        return DbActionTypeEnum.DELETE;
    }

    @Override
    public BinlogActionCheckSignDTO execute(BinlogActionProcessDTO<BinlogRowDataDTO> entity) {
        final BinlogRowDataDTO data = entity.getData();

        String message = this.checkSaltSignIsEmpty(data.getBeforeSalt(), data.getBeforeSign())
            ? messageSource.getMessage(BinlogConsumerCodeEnum.SALT_AND_SIGN_BOTH_EMPTY.getBizCode(), null,
            BinlogConsumerCodeEnum.SALT_AND_SIGN_BOTH_EMPTY.getBizMessage(), Locale.getDefault())
            : messageSource.getMessage(BinlogConsumerCodeEnum.DATA_DELETED.getBizCode(), null,
                BinlogConsumerCodeEnum.DATA_DELETED.getBizMessage(), Locale.getDefault());

        return BinlogActionCheckSignDTO.builder()
            .success(false)
            .message(message)
            .build();
    }
}
