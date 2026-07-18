/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import com.iwindplus.base.domain.vo.DbBaseTwoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 邮件日志视图对象.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "邮件日志视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailLogVO extends DbBaseTwoVO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 主题.
     */
    @Schema(description = "主题")
    private String subject;

    /**
     * 内容（必填）.
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 发件服务器账户.
     */
    @Schema(description = "发件服务器账户")
    private String username;

    /**
     * 发件人昵称.
     */
    @Schema(description = "发件人昵称")
    private String nickName;

    /**
     * 收件人（必填）.
     */
    @Schema(description = "收件人")
    private String tos;

    /**
     * 抄送人.
     */
    @Schema(description = "抄送人")
    private String ccs;

    /**
     * 密送人.
     */
    @Schema(description = "密送人")
    private String bccs;

    /**
     * 发送次数.
     */
    @Schema(description = "发送次数")
    private Integer sendCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 发送结果（0：失败，1：成功）.
     */
    @Schema(description = "发送结果（false：失败，true：成功）")
    private Boolean result;

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
}
