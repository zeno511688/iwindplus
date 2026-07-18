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
 * rocket 主题+tag key数据传输对象.
 *
 * @author zengdegui
 * @since 2026/04/07 22:39
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RocketTopicTagKeyDTO implements Serializable {

    /**
     * 主题名称.
     */
    private String topic;

    /**
     * 标签.
     */
    private String tag;
}

