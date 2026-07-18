/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码返回值枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
public enum BinlogConsumerCodeEnum implements CommonException {
    /**
     * 无效的操作策略.
     */
    INVALID_ACTION_STRATEGY("invalid_action_strategy", "无效的操作策略"),

    /**
     * 无效的数据源配置.
     */
    INVALID_DATA_SOURCE_CFG("invalid_data_source_cfg", "无效数据源配置"),

    /**
     * 数据的加签盐和签名同时为空.
     */
    SALT_AND_SIGN_BOTH_EMPTY("salt_and_sign_both_empty", "数据的加签盐和签名同时为空"),

    /**
     * 数据被删除.
     */
    DATA_DELETED("data_deleted", "数据被删除"),

    /**
     * 签名错误.
     */
    SIGN_ERROR("sign_error", "签名错误"),

    /**
     * 操作后的加签盐比操作前的小.
     */
    AFTER_SALT_SMALL_BEFORE_SALT("after_salt_small_before_salt", "操作后的加签盐比操作前的小"),

    /**
     * 操作后的加签盐未更新.
     */
    AFTER_SALT_NOT_UPDATED("after_salt_not_updated", "操作后的加签盐未更新");

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    BinlogConsumerCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
