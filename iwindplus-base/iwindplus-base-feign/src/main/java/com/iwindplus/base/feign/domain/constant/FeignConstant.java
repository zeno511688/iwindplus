/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.List;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class FeignConstant {

    private FeignConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 可预期的Feign异常（用于降级场景）
     */
    public static final List<Class<? extends Throwable>> EXPECTED_EXCEPTIONS = List.of(
        RetryableException.class,
        CallNotPermittedException.class,
        FeignException.class
    );
}
