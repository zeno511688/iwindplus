/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.iwindplus.setup.domain.dto.MailboxSendDTO;

/**
 * 邮箱业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
public interface MailboxService {

    /**
     * 发送邮件.
     *
     * @param entity 对象
     */
    void send(MailboxSendDTO entity);

    /**
     * 发送邮箱验证码（邮箱）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板配置编码（必填）
     * @param mail      邮箱（必填）
     */
    void sendCaptcha(String requestId, String tplCode, String mail);

    /**
     * 发送邮箱验证码（用户主键）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板配置编码（必填）
     * @param userId    用户主键（必填）
     * @param orgId     组织主键（必填）
     */
    void sendCaptchaByUserId(String requestId, String tplCode, Long userId, Long orgId);

}
