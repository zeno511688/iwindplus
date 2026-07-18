/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.domain.dto.ApiSignGenerateDTO;
import com.iwindplus.base.util.domain.dto.ApiSignVerifyDTO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * API签名工具类.
 *
 * @author zengdegui
 * @since 2021/1/11
 */
@Slf4j
public class ApiSignUtil {

    private ApiSignUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 生成签名.
     * </p>
     * 支持两种签名方式：
     * </p>
     * <p>
     * 1、HmacSHA256（推荐）
     * </p>
     * 适用于： 传入 secretKey 场景。
     * </p>
     * 待签名参数：
     * </p>
     * X-Access-Key（必填） X-Timestamp（必填） X-Nonce（必填） X-Method（必填） X-Path（可选） application（可选） params（可选）
     * </p>
     * 参数处理规则：
     * </p>
     * 1. params 中 null 值自动忽略。
     * </p>
     * 2. body/json 参数会递归展开（flatten）：
     * </p>
     * 原始 body： { "user": { "name": "zhangsan", "age": 18 }, "tags": ["a", "b"] }
     * </p>
     * 展开后： tags[0]=a tags[1]=b user.age=18 user.name=zhangsan
     * </p>
     * 3. 所有待签名参数统一放入 Map 后， 按照 key 字典序排序。
     * </p>
     * 4. value 统一进行 RFC3986（UTF-8）转义， 防止特殊字符导致签名不一致。
     * </p>
     * 示例：
     * </p>
     * name=张三&remark=a=b&msg=hello world
     * </p>
     * 转义后：
     * </p>
     * msg=hello+world &name=%E5%BC%A0%E4%B8%89 &remark=a%3Db
     * </p>
     * 5. 转义完成后按照： key1=value1&key2=value2 格式拼接待签名串。
     * </p>
     * 示例：
     * </p>
     * X-Access-Key=test &X-Method=POST &X-Nonce=1234567890 &X-Path=%2Fapi%2Fuser%2Fsave &X-Timestamp=1710000000000 &tags[0]=a &tags[1]=b &user.age=18
     * &user.name=zhangsan
     * </p>
     * 签名算法：
     * </p>
     * HmacSHA256(data, secretKey)
     * </p>
     * <p>
     * 2、SHA256
     * </p>
     * 适用于： 未传 secretKey 场景。
     * </p>
     * 参数规则与 HmacSHA256 完全一致。
     * </p>
     * 签名算法：
     * </p>
     * SHA256(data)
     *
     * @param entity 对象
     * @return String
     */
    public static String generateSign(ApiSignGenerateDTO entity) {
        ApiSignUtil.checkBaseParam(entity);

        return ApiSignUtil.buildSign(entity);
    }

    /**
     * 验证签名.
     *
     * @param entity 对象
     * @return boolean
     */
    public static boolean verifySign(ApiSignVerifyDTO entity) {
        ApiSignUtil.checkBaseParam(entity);
        ApiSignUtil.checkSignVerifyParam(entity);

        String sign = ApiSignUtil.buildSign(entity);
        return MessageDigest.isEqual(
            sign.getBytes(StandardCharsets.UTF_8),
            entity.getSign().getBytes(StandardCharsets.UTF_8)
        );
    }

    private static void checkBaseParam(ApiSignGenerateDTO entity) {
        // 时间戳
        final String timestamp = entity.getTimestamp();
        if (CharSequenceUtil.isBlank(timestamp)) {
            throw new BizException(BizCodeEnum.TIMESTAMP_NOT_EXIST);
        }

        if (!CharSequenceUtil.isNumeric(timestamp)) {
            throw new BizException(BizCodeEnum.ONLY_SUPPORT_NUMBER);
        }

        // 随机数
        final String nonce = entity.getNonce();
        if (CharSequenceUtil.isBlank(nonce) || nonce.length() < NumberConstant.NUMBER_TEN) {
            throw new BizException(BizCodeEnum.INVALID_NONCE);
        }

        // 请求方式
        final String method = entity.getMethod();
        if (CharSequenceUtil.isBlank(method)) {
            throw new BizException(BizCodeEnum.REQUEST_METHOD_NOT_EXIST);
        }
    }

    private static void checkSignVerifyParam(ApiSignVerifyDTO entity) {
        // 签名
        final String sign = entity.getSign();
        if (CharSequenceUtil.isBlank(sign)) {
            throw new BizException(BizCodeEnum.SIGN_NOT_EXIST);
        }

        // 超时时间
        final Duration timeout = entity.getTimeout();
        if (ObjectUtil.isEmpty(timeout)) {
            throw new BizException(BizCodeEnum.SIGN_TIMEOUT_NOT_EXIST);
        }

        final String timestamp = entity.getTimestamp();
        if (ApiSignUtil.isExpired(Long.valueOf(timestamp), timeout)) {
            throw new BizException(BizCodeEnum.SIGN_EXPIRED, new Object[]{entity.getTimeout().toSeconds()});
        }
    }

    private static boolean isExpired(long timestamp, Duration timeout) {
        long now = System.currentTimeMillis();
        long delta = now - timestamp;
        return delta < 0 || delta > timeout.toMillis();
    }

    private static String buildSign(ApiSignGenerateDTO entity) {
        final Map<String, Object> entityMap = new HashMap<>(16);
        entityMap.put(ApiSignConstant.X_TIMESTAMP, entity.getTimestamp());
        entityMap.put(ApiSignConstant.X_NONCE, entity.getNonce());
        entityMap.put(ApiSignConstant.X_METHOD, entity.getMethod());

        if (CharSequenceUtil.isNotBlank(entity.getAccessKey())) {
            entityMap.put(ApiSignConstant.X_ACCESS_KEY, entity.getAccessKey());
        }

        if (CharSequenceUtil.isNotBlank(entity.getPath())) {
            entityMap.put(ApiSignConstant.X_PATH, entity.getPath());
        }

        if (CharSequenceUtil.isNotBlank(entity.getApplication())) {
            entityMap.put(ApiSignConstant.APPLICATION, entity.getApplication());
        }

        final Map<String, Object> params = entity.getParams();
        if (ObjectUtil.isNotEmpty(params)) {
            final String jsonStr = JacksonUtil.toJsonStr(params);
            final Map<String, Object> flatten = JsonFlattener.flattenAsMap(jsonStr);
            entityMap.putAll(MapUtil.removeNullValue(flatten));
        }

        final String data = HttpsUtil.concatMap(entityMap);

        // 有 secretKey 用 HmacSHA256
        if (CharSequenceUtil.isNotBlank(entity.getSecretKey())) {
            HMac hmac = SecureUtil.hmac(
                HmacAlgorithm.HmacSHA256,
                entity.getSecretKey()
            );
            return hmac.digestHex(data);
        }

        // 无 secretKey 退化 sha256
        return SecureUtil.sha256(data);
    }
}