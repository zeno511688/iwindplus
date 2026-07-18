/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.util.DbSignUtil;
import com.iwindplus.base.util.domain.dto.DbSignVerifyDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.BinlogRowDataDTO;
import com.iwindplus.binlog.comsumer.server.domain.dto.SourceMetaDTO;
import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty;
import com.iwindplus.binlog.comsumer.server.strategy.BinlogActionStrategy;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象binlog 操作策略实现类.
 *
 * @param <T> 参数
 * @param <R> 结果
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public abstract class AbstractBinlogActionStrategyImpl<T, R> implements BinlogActionStrategy<T, R> {

    @Resource
    protected BinLogConsumerProperty property;

    /**
     * 验证签名是否正确.
     *
     * @param data 数据
     * @return boolean
     */
    protected boolean checkSignIsRight(BinlogRowDataDTO data) {
        final DbActionTypeEnum action = DbActionTypeEnum.fromAlias(data.getOp());
        if (Objects.isNull(action)) {
            log.warn("binlog 操作类型数据有误");
            return false;
        }

        final SourceMetaDTO source = data.getSource();
        if (Objects.isNull(source)) {
            log.warn("binlog 元数据有误");
            return false;
        }

        final Object sign = data.getAfterSign();
        if (Objects.isNull(sign)) {
            log.warn("binlog after中签名有误");
            return false;
        }

        // 反射获取数据
        DbSignVerifyDTO signVerifyDTO = DbSignVerifyDTO
            .builder()
            .secretKey(property.getSecretKey(source.getDb()))
            .salt(data.getAfterSalt())
            .dbName(source.getDb())
            .tableName(source.getTable())
            .action(action.name())
            .sign(sign.toString())
            .build();
        return DbSignUtil.verifySign(signVerifyDTO);
    }

    /**
     * 验证加签盐，签名数据是否为空.
     *
     * @param salt 数据
     * @param sign 签名
     * @return boolean
     */
    protected boolean checkSaltSignIsEmpty(Long salt, String sign) {
        if (Objects.nonNull(salt)
            && salt > 0L
            && CharSequenceUtil.isNotBlank(sign)) {
            return false;
        }
        return true;
    }
}
