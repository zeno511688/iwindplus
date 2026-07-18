/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.domain.dto.MailboxSendDTO;
import com.iwindplus.setup.server.service.MailboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 邮箱相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "邮箱接口")
@Slf4j
@RestController
@RequestMapping("admin/setup/mailbox")
@Validated
@RequiredArgsConstructor
public class MailboxController extends BaseController {

    private final MailboxService mailboxService;

    /**
     * 发送邮件.
     *
     * @param entity      对象
     * @param attachments 附件（可选）
     */
    @Operation(summary = "发送邮件")
    @PostMapping(value = "send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void send(
        @ModelAttribute @Valid MailboxSendDTO entity,
        @RequestPart(required = false) List<MultipartFile> attachments) {
        final String requestId = this.getRequestId();
        final List<UploadByteDTO> uploadBytes = FilesUtil.listUploadBytes(attachments);
        entity.setRequestId(requestId);
        entity.setAttachments(uploadBytes);
        this.mailboxService.send(entity);
    }

    /**
     * 发送邮箱验证码（邮箱）.
     *
     * @param tplCode 模板配置编码（必填）
     * @param mail    邮箱（必填）
     */
    @Operation(summary = "发送邮箱验证码（邮箱）")
    @PostMapping("sendCaptcha")
    public void sendCaptcha(
        @RequestParam String tplCode,
        @RequestParam String mail) {
        final String requestId = this.getRequestId();
        this.mailboxService.sendCaptcha(requestId, tplCode, mail);
    }

    /**
     * 发送邮箱验证码（用户主键）.
     *
     * @param tplCode 模板配置编码（必填）
     */
    @Operation(summary = "发送邮箱验证码（用户主键）")
    @PostMapping("sendCaptchaByUserId")
    public void sendCaptchaByUserId(@RequestParam String tplCode) {
        final String requestId = this.getRequestId();
        final Long userId = this.getUserInfo().getUserId();
        final Long orgId = this.getUserInfo().getOrgId();
        this.mailboxService.sendCaptchaByUserId(requestId, tplCode, userId, orgId);
    }
}
