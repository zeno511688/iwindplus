/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码返回值枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
public enum DtxCodeEnum implements CommonException {

    /**
     * 全局事务ID已经存在.
     */
    XID_EXIST("xid_exist", "全局事务ID已经存在"),

    ;

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
    DtxCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
