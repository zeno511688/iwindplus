/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import com.iwindplus.base.domain.dto.AkSkDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 通用 STS 临时令牌模型.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StsTokenDTO extends AkSkDTO {

    /**
     * sts地域节点（必填，如：sts.cn-shenzhen.aliyuncs.com）.
     */
    private String endpoint;

    /**
     * RAM角色（必填，（如 acs:ram::xxx:role/xxx））.
     */
    private String roleArn;

    /**
     * RAM权限策略（可选）.
     */
    private String policy;

    /**
     * 上传授权安全令牌（可选，会自动生成）.
     */
    private String securityToken;

    /**
     * 安全令牌过期时间（可选，会自动生成）
     */
    private LocalDateTime expiration;
}
