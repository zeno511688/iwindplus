/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 短信相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface SmsApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/sms/";

    /**
     * 发送手机验证码（手机）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板编码（必填）
     * @param mobile    手机（必填）
     */
    @Operation(summary = "发送手机验证码")
    @PostMapping(API_PREFIX + "sendCaptcha")
    void sendCaptcha(
        @RequestParam(value = "requestId", required = false) String requestId,
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "mobile") String mobile);

    /**
     * 发送手机验证码（用户主键）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板编码（必填）
     * @param userId    用户主键（必填）
     * @param orgId     组织主键（必填）
     */
    @Operation(summary = "发送手机验证码")
    @PostMapping(API_PREFIX + "sendCaptchaByUserId")
    void sendCaptchaByUserId(
        @RequestParam(value = "requestId", required = false) String requestId,
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "userId") Long userId,
        @RequestParam(value = "orgId") Long orgId);

}
