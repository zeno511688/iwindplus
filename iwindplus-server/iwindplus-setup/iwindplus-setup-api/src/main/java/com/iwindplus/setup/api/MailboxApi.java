/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.api;

import com.iwindplus.setup.domain.dto.MailboxSendDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 邮箱相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface MailboxApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/mailbox/";

    /**
     * 发送邮件.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送邮件")
    @PostMapping(value = API_PREFIX + "send")
    void send(@RequestBody @Validated MailboxSendDTO entity);

    /**
     * 发送邮箱验证码（邮箱）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板配置编码（必填）
     * @param mail      邮箱（必填）
     */
    @Operation(summary = "发送邮箱验证码（邮箱）（模板参数固定为：captcha和timeout）")
    @PostMapping(API_PREFIX + "sendCaptcha")
    void sendCaptcha(
        @RequestParam(value = "requestId", required = false) String requestId,
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "mail") String mail);

    /**
     * 发送邮箱验证码（用户主键）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板配置编码（必填）
     * @param userId    用户主键（必填）
     * @param orgId     组织主键（必填）
     */
    @Operation(summary = "发送邮箱验证码（用户主键）（模板参数固定为：captcha和timeout）")
    @PostMapping(API_PREFIX + "sendCaptchaByUserId")
    void sendCaptchaByUserId(
        @RequestParam(value = "requestId", required = false) String requestId,
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "userId") Long userId,
        @RequestParam(value = "orgId") Long orgId);

}
