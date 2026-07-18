/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 算法枚举.
 *
 * @author zengdegui
 * @since 2024/10/11
 */
@Getter
@RequiredArgsConstructor
public enum AlgorithmTypeEnum implements BaseEnum<String> {

    /**
     * base64.
     */
    BASE64("base64", "base64算法"),

    /**
     * aes.
     */
    AES("aes", "aes算法"),

    /**
     * rsa.
     */
    RSA("rsa", "rsa算法"),

    /**
     * sm2.
     */
    SM2("sm2", "sm2算法"),

    /**
     * sm4.
     */
    SM4("sm4", "sm4算法"),

    /**
     * sm3.
     */
    SM3("sm3", "sm3算法"),

    /**
     * sha256.
     */
    SHA256("sha256", "sha256算法"),

    /**
     * md5.
     */
    MD5("md5", "md5算法");

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
