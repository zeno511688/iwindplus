/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SumSub业务编码枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
@RequiredArgsConstructor
public enum SumSubCodeEnum implements CommonException {

    /**
     * 请求ID不能为空.
     */
    REQUEST_ID_NOT_EMPTY("request_id_not_empty", "请求ID不能为空"),


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
     * 通过业务编码查找枚举.
     *
     * @param bizCode 业务编码
     * @return BizCodeEnum
     */
    public static SumSubCodeEnum valueOfBizCode(String bizCode) {
        final Stream<SumSubCodeEnum> stream = Arrays.stream(SumSubCodeEnum.values());
        return stream.filter(m -> Objects.equals(bizCode, m.getBizCode())).findFirst().orElse(null);
    }

}
