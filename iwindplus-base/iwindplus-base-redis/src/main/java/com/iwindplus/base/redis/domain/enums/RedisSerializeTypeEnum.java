/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 序列化类型枚举. 序列化数据的大小方面：kryo < protostuff < jackson < jdk 反序列化速度方面：kryo > protostuff > jackson > jdk
 *
 * @author zengdegui
 * @since 2024/10/11
 */
@Getter
@RequiredArgsConstructor
public enum RedisSerializeTypeEnum implements BaseEnum<String> {

    /**
     * kryo.
     */
    KRYO("kryo", "性能高，适合高性能场景，二进制格式，数据体积最小"),

    /**
     * protostuff.
     */
    PROTOSTUFF("protostuff", "性能高，序列化/反序列化速度快，二进制格式，数据积体较小，支持多种格式"),

    /**
     * jackson.
     */
    JACKSON("jackson", "性能相对较低，json格式，数据体积最大，广泛支持，兼容性强"),

    /**
     * jdk.
     */
    JDK("jdk", "性能差，要序列化的信息太多导致序列化后的二进制流太大，传输成本大");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;

}
