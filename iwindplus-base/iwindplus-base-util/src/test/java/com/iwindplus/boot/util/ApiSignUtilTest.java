/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.boot.util;

import cn.hutool.core.lang.Assert;
import com.iwindplus.base.util.ApiSignUtil;
import com.iwindplus.base.util.domain.dto.ApiSignGenerateDTO;
import com.iwindplus.base.util.domain.dto.ApiSignVerifyDTO;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * API签名工具测试类.
 *
 * @author zengdegui
 * @since 2025/07/06 14:50
 */
public class ApiSignUtilTest {

    private String secretKey = "test-secret-key";
    private String accessKey = "test-access-key";
    private String timestamp = System.currentTimeMillis() + "";
    private String nonce = "test-nonce-1234567890";
    private String path = "/api/test";
    private String method = "GET";

    @BeforeEach
    void setUp() {
        // 可以在此初始化一些通用数据（如果需要）
    }

    /**
     * 测试正常签名生成与验证流程.
     */
    @Test
    void testGenerateAndVerifySignSuccess() {
        // 准备参数
        ApiSignGenerateDTO generateDTO = new ApiSignGenerateDTO();
        generateDTO.setAccessKey(accessKey);
        generateDTO.setTimestamp(timestamp);
        generateDTO.setNonce(nonce);
        generateDTO.setPath(path);
        generateDTO.setMethod(method);
        generateDTO.setSecretKey(secretKey);

        // 添加请求参数
        Map<String, Object> params = new HashMap<>(16);
        params.put("key1", "value1");
        params.put("key2", "value2");
        generateDTO.setParams(params);

        // 生成签名
        String sign = ApiSignUtil.generateSign(generateDTO);

        // 验证签名
        ApiSignVerifyDTO verifyDTO = new ApiSignVerifyDTO();
        verifyDTO.setAccessKey(accessKey);
        verifyDTO.setTimestamp(timestamp);
        verifyDTO.setNonce(nonce);
        verifyDTO.setPath(path);
        verifyDTO.setMethod(method);
        verifyDTO.setSecretKey(secretKey);
        verifyDTO.setSign(sign);
        Map<String, Object> params2 = new HashMap<>(16);
        params2.put("key2", "value2");
        params2.put("key1", "value1");
        verifyDTO.setParams(params2);
        verifyDTO.setTimeout(Duration.ofSeconds(60));
        final boolean data = ApiSignUtil.verifySign(verifyDTO);

        Assert.isTrue(data);
    }

}
