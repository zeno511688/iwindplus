/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.domain.dto;

import com.iwindplus.base.domain.dto.UploadByteDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 邮件发送数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Schema(description = "邮件发送数据传输对象")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MailDTO implements Serializable {

    /**
     * 邮件主题（必填）.
     */
    @Schema(description = "邮件主题")
    @NotBlank(message = "{subject.notEmpty}")
    private String subject;

    /**
     * 邮件内容（必填）.
     */
    @Schema(description = "邮件内容")
    @NotBlank(message = "{content.notEmpty}")
    private String content;

    /**
     * 收件人（必填）.
     */
    @NotEmpty(message = "{tos.notEmpty}")
    @Schema(description = "收件人")
    private List<String> tos;

    /**
     * 抄送人.
     */
    @Schema(description = "抄送人")
    private List<String> ccs;

    /**
     * 密送人.
     */
    @Schema(description = "密送人")
    private List<String> bccs;

    /**
     * 附件.
     */
    @Schema(description = "附件")
    private List<UploadByteDTO> attachments;

    /**
     * 是否是html方式.
     */
    @Schema(description = "是否是html方式")
    private Boolean html;
}
