/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * offSet 管理key.
 *
 * @author zengdegui
 * @since 2026/07/28 16:29
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OffsetManagerKeyDTO implements Serializable {

    /**
     * kafka集群
     */
    private String cluster;

    /**
     * consumer group
     */
    private String groupId;

    /**
     * listener唯一ID
     */
    private String listenerId;
}
