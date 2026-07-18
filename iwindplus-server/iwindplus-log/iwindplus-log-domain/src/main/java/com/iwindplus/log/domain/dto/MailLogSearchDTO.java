/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 邮箱日志搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "邮箱日志搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailLogSearchDTO extends DbPageDTO {

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
     * 主题.
     */
    @Schema(description = "主题")
    @Length(max = 255, message = "{moduleName.length}")
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
    @Length(max = 100, message = "{username.length}")
    private String username;

    /**
     * 发件人昵称.
     */
    @Schema(description = "发件人昵称")
    @Length(max = 100, message = "{nickName.length}")
    private String nickName;

    /**
     * 收件人.
     */
    @Schema(description = "收件人")
    @Length(max = 100, message = "{tos.length}")
    private String tos;

    /**
     * 发送状态（false：失败，true：成功）.
     */
    @Schema(description = "发送状态（false：失败，true：成功）")
    private Boolean result;

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
