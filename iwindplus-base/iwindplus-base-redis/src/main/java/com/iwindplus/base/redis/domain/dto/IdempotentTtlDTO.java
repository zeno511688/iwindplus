/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.dto;

import java.io.Serializable;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 幂等ttl数据传输对象.
 *
 * @author zengdegui
 * @since 2026/04/03 20:46
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotentTtlDTO implements Serializable {

    /**
     * 幂等处理中过期时间.
     */
    private Duration processingTtl;

    /**
     * 幂等成功过期时间.
     */
    private Duration successTtl;
}
