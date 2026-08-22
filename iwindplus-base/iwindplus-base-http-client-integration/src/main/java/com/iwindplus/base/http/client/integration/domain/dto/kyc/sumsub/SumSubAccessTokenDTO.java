/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SumSub 访问令牌请求.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumSubAccessTokenDTO implements Serializable {

    /**
     * 外部用户ID（必填）.
     */
    private String externalUserId;

    /**
     * 过期时间（秒），默认为30天.
     */
    private Integer ttlInSecs;

    /**
     * 安全级别.
     */
    private String levelName;
}
