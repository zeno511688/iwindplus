/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.DbSignConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.domain.dto.DbSignGenerateDTO;
import com.iwindplus.base.util.domain.dto.DbSignVerifyDTO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库签名相关工具类.
 *
 * @author zengdegui
 * @since 2025/11/20 23:34
 */
@Slf4j
public class DbSignUtil {

    private DbSignUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 生成签名.
     * </p>
     * 第一步，加密串=按照字典序排序，拼接成字符串，例如："key1=value1&key2=value2"
     * </p>
     * 第二步，签名=HmacSHA256(加密串)
     *
     * @param entity 对象
     * @return String
     */
    public static String generateSign(DbSignGenerateDTO entity) {
        DbSignUtil.checkBaseParam(entity);

        return DbSignUtil.buildSign(entity);
    }

    /**
     * 验证签名.
     *
     * @param entity 对象
     * @return boolean
     */
    public static boolean verifySign(DbSignVerifyDTO entity) {
        DbSignUtil.checkBaseParam(entity);
        DbSignUtil.checkSignVerifyParam(entity);

        String sign = DbSignUtil.buildSign(entity);
        return MessageDigest.isEqual(
            sign.getBytes(StandardCharsets.UTF_8),
            entity.getSign().getBytes(StandardCharsets.UTF_8)
        );
    }

    private static void checkBaseParam(DbSignGenerateDTO entity) {
        // 密钥
        final String secretKey = entity.getSecretKey();
        if (CharSequenceUtil.isBlank(secretKey)) {
            throw new BizException(BizCodeEnum.SECRET_KEY_NOT_EXIST);
        }

        // 加密盐
        final Long salt = entity.getSalt();
        if (Objects.isNull(salt)) {
            throw new BizException(BizCodeEnum.SALT_NOT_EXIST);
        }

        // 数据库名称
        final String dbName = entity.getDbName();
        if (CharSequenceUtil.isBlank(dbName)) {
            throw new BizException(BizCodeEnum.DB_NAME_NOT_EXIST);
        }

        // 表名
        final String tableName = entity.getTableName();
        if (CharSequenceUtil.isBlank(tableName)) {
            throw new BizException(BizCodeEnum.TABLE_NAME_NOT_EXIST);
        }

        // 操作
        final String action = entity.getAction();
        if (CharSequenceUtil.isBlank(action)) {
            throw new BizException(BizCodeEnum.ACTION_NOT_EXIST);
        }
    }

    private static void checkSignVerifyParam(DbSignVerifyDTO entity) {
        // 签名
        final String sign = entity.getSign();
        if (CharSequenceUtil.isBlank(sign)) {
            throw new BizException(BizCodeEnum.SIGN_NOT_EXIST);
        }
    }

    private static String buildSign(DbSignGenerateDTO entity) {
        final Map<String, Object> entityMap = new HashMap<>(16);
        entityMap.put(DbSignConstant.SALT, entity.getSalt());
        entityMap.put(DbSignConstant.DB_NAME, entity.getDbName());
        entityMap.put(DbSignConstant.TABLE_NAME, entity.getTableName());
        entityMap.put(DbSignConstant.ACTION, entity.getAction());

        final String data = HttpsUtil.concatMap(entityMap);

        HMac hmac = SecureUtil.hmac(
            HmacAlgorithm.HmacSHA256,
            entity.getSecretKey()
        );
        return hmac.digestHex(data);
    }
}
