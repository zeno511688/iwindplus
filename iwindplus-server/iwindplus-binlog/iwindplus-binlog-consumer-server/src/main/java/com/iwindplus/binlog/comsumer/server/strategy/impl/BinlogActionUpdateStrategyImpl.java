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
 * 抽象binlog 操作策略实现类（修改）.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Component
public class BinlogActionUpdateStrategyImpl extends AbstractBinlogActionStrategyImpl<BinlogRowDataDTO, BinlogActionCheckSignDTO> {

    @Resource
    private MessageSource messageSource;

    @Override
    public DbActionTypeEnum support() {
        return DbActionTypeEnum.UPDATE;
    }

    @Override
    public BinlogActionCheckSignDTO execute(BinlogActionProcessDTO<BinlogRowDataDTO> entity) {
        final BinlogRowDataDTO data = entity.getData();

        final Long beforeSalt = data.getBeforeSalt();
        final Long afterSalt = data.getAfterSalt();
        final String beforeSign = data.getBeforeSign();
        final String afterSign = data.getAfterSign();

        if (this.checkSaltSignIsEmpty(beforeSalt, beforeSign) && this.checkSaltSignIsEmpty(afterSalt, afterSign)) {
            return BinlogActionCheckSignDTO.builder()
                .success(false)
                .message(messageSource.getMessage(BinlogConsumerCodeEnum.SALT_AND_SIGN_BOTH_EMPTY.getBizCode(), null,
                    BinlogConsumerCodeEnum.SALT_AND_SIGN_BOTH_EMPTY.getBizMessage(), Locale.getDefault()))
                .build();
        }

        // 验证加签盐是否增大
        if (afterSalt < beforeSalt) {
            return BinlogActionCheckSignDTO.builder()
                .success(false)
                .message(messageSource.getMessage(BinlogConsumerCodeEnum.AFTER_SALT_SMALL_BEFORE_SALT.getBizCode(), null,
                    BinlogConsumerCodeEnum.AFTER_SALT_SMALL_BEFORE_SALT.getBizMessage(), Locale.getDefault()))
                .build();
        }

        // 验证加签盐是否更新
        if (afterSalt.equals(beforeSalt)) {
            return BinlogActionCheckSignDTO.builder()
                .success(false)
                .message(messageSource.getMessage(BinlogConsumerCodeEnum.AFTER_SALT_NOT_UPDATED.getBizCode(), null,
                    BinlogConsumerCodeEnum.AFTER_SALT_NOT_UPDATED.getBizMessage(), Locale.getDefault()))
                .build();
        }

        // 验证签名是否正确
        if (!this.checkSignIsRight(data)) {
            return BinlogActionCheckSignDTO.builder()
                .success(false)
                .message(messageSource.getMessage(BinlogConsumerCodeEnum.SIGN_ERROR.getBizCode(), null,
                    BinlogConsumerCodeEnum.SIGN_ERROR.getBizMessage(), Locale.getDefault()))
                .build();
        }

        return BinlogActionCheckSignDTO
            .builder()
            .success(true)
            .build();
    }
}
