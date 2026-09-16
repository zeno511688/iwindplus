/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.controller;

import com.iwindplus.base.domain.dto.UploadFileDTO;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.dto.MailboxSendDTO;
import com.iwindplus.integr.application.service.MailboxApplicationService;
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
@RequestMapping("admin/integr/mailbox")
@Validated
@RequiredArgsConstructor
public class MailboxController extends BaseController {

    private final MailboxApplicationService mailboxApplicationService;

    public static final String MAIL_TPL_CODE = "c3f67fd355dd6098156053f68285ba3e";

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
        final List<UploadFileDTO> uploadList = FilesUtil.listUploadBytes(attachments);
        entity.setAttachments(uploadList);
        this.mailboxApplicationService.send(entity);
    }

    /**
     * 发送邮箱验证码（用户主键）.
     */
    @Operation(summary = "发送邮箱验证码（用户主键）")
    @PostMapping("sendCaptchaByUserId")
    public void sendCaptchaByUserId() {
        final Long userId = this.getUserInfo().getUserId();
        final Long orgId = this.getUserInfo().getOrgId();
        final String tplCode = MAIL_TPL_CODE;
        this.mailboxApplicationService.sendCaptchaByUserId(tplCode, userId, orgId);
    }

    /**
     * 发送邮箱验证码（邮箱）.
     *
     * @param mail 邮箱（必填）
     */
    @Operation(summary = "发送邮箱验证码（邮箱）")
    @PostMapping("sendCaptcha")
    public void sendCaptcha(
        @RequestParam String mail) {
        final String tplCode = MAIL_TPL_CODE;
        this.mailboxApplicationService.sendCaptcha(tplCode, mail);
    }
}
