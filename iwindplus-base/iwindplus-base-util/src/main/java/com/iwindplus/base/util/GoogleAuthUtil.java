/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.base.util.domain.vo.GoogleAuthVO;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import java.util.Optional;

/**
 * 谷歌验证器相关工具类.
 *
 * @author zengdegui
 * @since 2025/04/19 00:40
 */
public class GoogleAuthUtil {

    private GoogleAuthUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    private static final GoogleAuthenticator GOOGLE_AUTH = new GoogleAuthenticator();
    private static final int DEFAULT_SIZE = 300;

    /**
     * 生成密钥.
     *
     * @return String
     */
    public static GoogleAuthenticatorKey generateCredentials() {
        return GOOGLE_AUTH.createCredentials();
    }

    /**
     * 验证用户输入的验证码是否正确.
     *
     * @param secret 密钥
     * @param code   验证码
     * @return boolean
     */
    public static boolean validate(String secret, int code) {
        return GOOGLE_AUTH.authorize(secret, code);
    }

    /**
     * 生成密钥和对应的二维码.
     *
     * @param issuer   发行方
     * @param userName 用户名
     * @return GoogleAuthVO
     */
    public static GoogleAuthVO generateQrUrl(String issuer, String userName) {
        final GoogleAuthenticatorKey credentials = GoogleAuthUtil.generateCredentials();
        final String content = GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, userName, credentials);
        return GoogleAuthVO.builder()
            .key(credentials.getKey())
            .content(content)
            .build();
    }

    /**
     * 生成密钥和对应的二维码内容.
     *
     * @param issuer   发行方
     * @param userName 用户名
     * @return GoogleAuthVO
     */
    public static GoogleAuthVO generateQrContent(String issuer, String userName) {
        final GoogleAuthenticatorKey credentials = GoogleAuthUtil.generateCredentials();
        final String content = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, userName, credentials);
        return GoogleAuthVO.builder()
            .key(credentials.getKey())
            .content(content)
            .build();
    }

    /**
     * 生成密钥和对应的二维码图片.
     *
     * @param issuer   发行方
     * @param userName 用户名
     * @param width    宽度
     * @param height   高度
     * @return GoogleAuthVO
     */
    public static GoogleAuthVO generateImage(String issuer, String userName, Integer width, Integer height) {
        final GoogleAuthVO data = GoogleAuthUtil.generateQrContent(issuer, userName);
        final String content = data.getContent();
        width = Optional.ofNullable(width).orElse(DEFAULT_SIZE);
        height = Optional.ofNullable(height).orElse(DEFAULT_SIZE);
        final byte[] generatePng = QrCodeUtil.generatePng(content, width, height);
        final String image = new StringBuilder(FileTypeEnum.PNG.getPrefix()).append(Base64.encode(generatePng)).toString();
        data.setContent(image);
        return data;
    }

}
