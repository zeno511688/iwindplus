/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.domain.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Rabbit 消费者key数据传输对象.
 *
 * @author zengdegui
 * @since 2026/04/07 22:39
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RocketConsumerKeyDTO implements Serializable {

    /**
     * 集群名称.
     */
    private String cluster;

    /**
     * 分组名称.
     */
    private String group;
}
