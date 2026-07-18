/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

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
import org.hibernate.validator.constraints.Length;

/**
 * 邮箱发送数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "邮箱发送数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailboxSendDTO implements Serializable {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Length(max = 100, message = "{requestId.length}")
    private String requestId;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    @NotBlank(message = "{code.notEmpty}")
    @Length(max = 100, message = "{code.length}")
    private String code;

    /**
     * 邮件主题（必填）.
     */
    @Schema(description = "邮件主题")
    @NotBlank(message = "{subject.notEmpty}")
    @Length(max = 100, message = "{subject.length}")
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
    @Schema(description = "收件人（必填）")
    @NotEmpty(message = "{tos.notEmpty}")
    private List<String> tos;

    /**
     * 抄送人（可选）.
     */
    @Schema(description = "抄送人（可选）")
    @NotEmpty(message = "{ccs.notEmpty}")
    private List<String> ccs;

    /**
     * 密送人（可选）.
     */
    @Schema(description = "密送人（可选）")
    @NotEmpty(message = "{bccs.notEmpty}")
    private List<String> bccs;

    /**
     * 附件（可选）.
     */
    @Schema(description = "附件")
    private List<UploadByteDTO> attachments;
}