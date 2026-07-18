/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.AES;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.AlgorithmTypeEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.domain.constant.UtilConstant.CryptoConstant;
import com.iwindplus.base.util.domain.dto.CryptoDTO;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 加解密安全相关工具类.
 *
 * @author zengdegui
 * @since 2025/04/19 00:40
 */
@Slf4j
public class CryptoUtil {

    /**
     * 线程安全的安全随机源，用于生成 AES-GCM 的随机 IV.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CryptoUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 加密（聚合）.
     *
     * @param data   待加密数据
     * @param config 加解密配置
     * @return String
     */
    public static String encrypt(String data, CryptoDTO config) {
        if (Boolean.FALSE.equals(config.getEnabled())) {
            return data;
        }

        final AlgorithmTypeEnum algorithm = config.getAlgorithm();
        if (ObjectUtil.isEmpty(algorithm)) {
            throw new BizException(BizCodeEnum.CRYPTO_ALGORITHM_NOT_SUPPORT);
        }

        try {
            return encrypt(data, config, algorithm);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("加密失败，算法={}, 数据={}", algorithm, data, e);
            throw new BizException(BizCodeEnum.ENCRYPT_ERROR);
        }
    }

    /**
     * 解密（聚合）.
     *
     * @param data   待解密数据
     * @param config 加解密配置
     * @return String
     */
    public static String decrypt(String data, CryptoDTO config) {
        if (Boolean.FALSE.equals(config.getEnabled())) {
            return data;
        }

        final AlgorithmTypeEnum algorithm = config.getAlgorithm();
        if (ObjectUtil.isEmpty(algorithm)) {
            throw new BizException(BizCodeEnum.CRYPTO_ALGORITHM_NOT_SUPPORT);
        }

        try {
            return decrypt(data, config, algorithm);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("解密失败，算法={}, 数据={}", algorithm, data, e);
            throw new BizException(BizCodeEnum.DECRYPT_ERROR);
        }
    }

    /**
     * Base64加密.
     *
     * @param data 待加密数据
     * @return String
     */
    public static String encryptByBase64(String data) {
        return Base64.encode(data);
    }

    /**
     * Base64解密.
     *
     * @param data 待解密数据
     * @return String
     */
    public static String decryptByBase64(String data) {
        return Base64.decodeStr(data);
    }

    /**
     * AES加密（AES/GCM/NoPadding）.
     *
     * <p>每次加密都会用 {@link SecureRandom} 生成一个全新的随机 IV，并将
     * {@code IV(12字节) + 密文+GCM认证标签} 拼接后做 Base64 编码返回，
     * 从根本上杜绝 GCM nonce（IV）重用。解密时从密文头部还原 IV。</p>
     *
     * @param data 待加密数据
     * @param key  密钥（32位）
     * @return String
     */
    public static String encryptByAes(String data, String key) {
        final byte[] keyBytes = CryptoUtil.validateAesKey(key);
        final byte[] iv = new byte[CryptoConstant.AES_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        final AES aes = new AES(CryptoConstant.AES_MODE, CryptoConstant.AES_PADDING, keyBytes, iv);
        final byte[] cipher = aes.encrypt(data.getBytes(StandardCharsets.UTF_8));
        final byte[] combined = new byte[iv.length + cipher.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipher, 0, combined, iv.length, cipher.length);
        return Base64.encode(combined);
    }

    /**
     * AES解密（AES/GCM/NoPadding）.
     *
     * <p>与 {@link #encryptByAes(String, String)} 对应：先 Base64 解码，
     * 取头部 12 字节作为 IV，其余部分为密文+认证标签。</p>
     *
     * @param data 待解密数据（Base64，含前置IV）
     * @param key  密钥（32位）
     * @return String
     */
    public static String decryptByAes(String data, String key) {
        final byte[] keyBytes = CryptoUtil.validateAesKey(key);
        final byte[] combined = Base64.decode(data);
        if (combined.length <= CryptoConstant.AES_IV_LENGTH) {
            throw new BizException(BizCodeEnum.INVALID_AES_IV);
        }
        final byte[] iv = new byte[CryptoConstant.AES_IV_LENGTH];
        final byte[] cipher = new byte[combined.length - CryptoConstant.AES_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, cipher, 0, cipher.length);
        final AES aes = new AES(CryptoConstant.AES_MODE, CryptoConstant.AES_PADDING, keyBytes, iv);
        final byte[] plain = aes.decrypt(cipher);
        return new String(plain, StandardCharsets.UTF_8);
    }

    /**
     * sm4加密.
     *
     * @param data 待加密数据
     * @param key  密钥（16位）
     * @return String
     */
    public static String encryptBySm4(String data, String key) {
        if (key == null || key.length() != CryptoConstant.SM4_KEY_LENGTH) {
            throw new BizException(BizCodeEnum.INVALID_SM4_KEY);
        }
        return SmUtil.sm4(key.getBytes(StandardCharsets.UTF_8)).encryptBase64(data);
    }

    /**
     * sm4解密.
     *
     * @param data 待解密数据
     * @param key  密钥（16位）
     * @return String
     */
    public static String decryptBySm4(String data, String key) {
        if (key == null || key.length() != CryptoConstant.SM4_KEY_LENGTH) {
            throw new BizException(BizCodeEnum.INVALID_SM4_KEY);
        }
        return SmUtil.sm4(key.getBytes(StandardCharsets.UTF_8)).decryptStr(data);
    }

    /**
     * 产生sm2加解密需要的公钥和私钥.
     *
     * @return Map<String, String>
     */
    public static Map<String, String> generateSm2Key() {
        Map<String, String> keyMap = new HashMap<>(16);
        SM2 sm2 = SmUtil.sm2();
        keyMap.put(CryptoConstant.PRIVATE_KEY, sm2.getPrivateKeyBase64());
        keyMap.put(CryptoConstant.PUBLIC_KEY, sm2.getPublicKeyBase64());
        return keyMap;
    }

    /**
     * sm2公钥加密.
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return String
     */
    public static String encryptBySm2(String data, String publicKey) {
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * sm2私钥解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return String
     */
    public static String decryptBySm2(String data, String privateKey) {
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return sm2.decryptStr(data, KeyType.PrivateKey);
    }

    /**
     * 产生RSA加解密需要的公钥和私钥.
     *
     * @return Map<String, String>
     */
    public static Map<String, String> generateRsaKey() {
        Map<String, String> keyMap = new HashMap<>(16);
        RSA rsa = SecureUtil.rsa();
        keyMap.put(CryptoConstant.PRIVATE_KEY, rsa.getPrivateKeyBase64());
        keyMap.put(CryptoConstant.PUBLIC_KEY, rsa.getPublicKeyBase64());
        return keyMap;
    }

    /**
     * rsa公钥加密.
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return String
     */
    public static String encryptByRsa(String data, String publicKey) {
        RSA rsa = SecureUtil.rsa(null, publicKey);
        return rsa.encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * rsa私钥解密.
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return String
     */
    public static String decryptByRsa(String data, String privateKey) {
        RSA rsa = SecureUtil.rsa(privateKey, null);
        return rsa.decryptStr(data, KeyType.PrivateKey);
    }

    /**
     * md5加密.
     *
     * @param data 待加密数据
     * @return String
     */
    public static String encryptByMd5(String data) {
        return SecureUtil.md5(data);
    }

    /**
     * sha256加密.
     *
     * @param data 待加密数据
     * @return String
     */
    public static String encryptBySha256(String data) {
        return SecureUtil.sha256(data);
    }

    /**
     * sm3加密.
     *
     * @param data 待加密数据
     * @return String
     */
    public static String encryptBySm3(String data) {
        return SmUtil.sm3(data);
    }

    private static byte[] validateAesKey(String key) {
        if (key == null || key.length() != CryptoConstant.AES_KEY_LENGTH) {
            throw new BizException(BizCodeEnum.INVALID_AES_KEY);
        }
        return key.getBytes(StandardCharsets.UTF_8);
    }

    private static String encrypt(String data, CryptoDTO config, AlgorithmTypeEnum algorithm) {
        switch (algorithm) {
            case BASE64:
                return CryptoUtil.encryptByBase64(data);
            case AES:
                return CryptoUtil.encryptByAes(data, config.getKey());
            case RSA:
                return CryptoUtil.encryptByRsa(data, config.getPublicKey());
            case SM2:
                return CryptoUtil.encryptBySm2(data, config.getPublicKey());
            case SM4:
                return CryptoUtil.encryptBySm4(data, config.getKey());
            default:
                throw new BizException(BizCodeEnum.CRYPTO_ALGORITHM_NOT_SUPPORT);
        }
    }

    private static String decrypt(String data, CryptoDTO config, AlgorithmTypeEnum algorithm) {
        switch (algorithm) {
            case BASE64:
                return CryptoUtil.decryptByBase64(data);
            case AES:
                return CryptoUtil.decryptByAes(data, config.getKey());
            case RSA:
                return CryptoUtil.decryptByRsa(data, config.getPrivateKey());
            case SM2:
                return CryptoUtil.decryptBySm2(data, config.getPrivateKey());
            case SM4:
                return CryptoUtil.decryptBySm4(data, config.getKey());
            default:
                throw new BizException(BizCodeEnum.CRYPTO_ALGORITHM_NOT_SUPPORT);
        }
    }
}
