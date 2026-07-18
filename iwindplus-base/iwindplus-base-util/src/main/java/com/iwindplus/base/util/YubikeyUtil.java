/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

/**
 * yubikey 校验工具类.
 *
 * @author zengdegui
 * @since 2026/04/26 15:01
 */
@Slf4j
public class YubikeyUtil {

    private YubikeyUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 校验签名.
     *
     * @param publicKey 公钥
     * @param source    源数据
     * @param sign      签名数据
     * @return boolean
     */
    public static boolean verifySign(String publicKey, String source, String sign) {
        try {
            // 解析公钥
            PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
                PGPUtil.getDecoderStream(new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8))),
                new JcaKeyFingerprintCalculator()
            );

            // 解析签名
            PGPObjectFactory pgpFact = new PGPObjectFactory(
                new ByteArrayInputStream(sign.getBytes(StandardCharsets.UTF_8)),
                new JcaKeyFingerprintCalculator());
            PGPSignatureList signatureList = null;
            Object obj;
            while ((obj = pgpFact.nextObject()) != null) {
                if (obj instanceof PGPSignatureList) {
                    signatureList = (PGPSignatureList) obj;
                    break;
                }
            }

            if (signatureList == null || signatureList.isEmpty()) {
                log.warn("No signatures found.");
                return false;
            }

            PGPSignature sig = signatureList.get(0);

            // 查找公钥
            PGPPublicKey key = pgpPub.getPublicKey(sig.getKeyID());
            if (key == null) {
                log.warn("Cannot find public key for keyID: " + sig.getKeyID());
                return false;
            }

            // 初始化验证器
            sig.init(new JcaPGPContentVerifierBuilderProvider()
                .setProvider(new BouncyCastleProvider()), key);

            // 更新数据
            sig.update(source.getBytes(StandardCharsets.UTF_8));

            // 验证签名
            return sig.verify();
        } catch (PGPException | IOException ex) {
            log.warn("yubikey verify sign failed", ex);
            throw new BizException(BizCodeEnum.YUBIKEY_VERIFY_ERROR);
        }
    }
}
