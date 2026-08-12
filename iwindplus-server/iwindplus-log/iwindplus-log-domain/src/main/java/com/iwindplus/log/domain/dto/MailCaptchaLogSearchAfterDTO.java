/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 邮箱验证码日志搜索数据传输对象(search_after深分页).
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Schema(description = "邮箱验证码日志搜索数据传输对象(search_after深分页)")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailCaptchaLogSearchAfterDTO implements Serializable {

    /**
     * 每页显示条数.
     */
    @Schema(description = "每页显示条数")
    private Integer size;

    /**
     * 下一页游标.
     */
    @Schema(description = "下一页游标")
    private List<Object> searchAfter;

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Length(max = 100, message = "{bizNumber.length}")
    private String bizNumber;

    /**
     * 邮件模板编码.
     */
    @Schema(description = "邮件模板编码")
    @Length(max = 100, message = "{tplCode.length}")
    private String tplCode;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @Length(max = 100, message = "{mail.length}")
    private String mail;

    /**
     * 工号
     */
    @Schema(description = "工号")
    @Length(max = 100, message = "{jobNumber.length}")
    private String jobNumber;

    /**
     * 用户手机.
     */
    @Schema(description = "用户手机")
    @Length(max = 100, message = "{mobile.length}")
    private String mobile;
}
