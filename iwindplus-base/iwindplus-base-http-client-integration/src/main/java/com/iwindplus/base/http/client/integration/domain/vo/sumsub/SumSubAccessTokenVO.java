/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.vo.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SumSub 访问令牌响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubAccessTokenVO implements Serializable {

    /**
     * 访问令牌.
     */
    private String token;

    /**
     * 外部用户ID.
     */
    private String externalUserId;

    /**
     * 过期时间戳（毫秒）.
     */
    private Long expiredAt;
}
