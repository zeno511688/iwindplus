/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.SendStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 系统通知消息搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "系统通知消息搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SysNoticeMsgSearchDTO implements Serializable {

    /**
     * 管理服务对象存储模板配置编码.
     */
    @Schema(description = "管理服务对象存储模板配置编码")
    private String mgtOssTplCode;

    /**
     * 标题.
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 发送人主键.
     */
    @Schema(description = "发送人主键")
    private Long senderId;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    private SendStatusEnum sendStatus;

    /**
     * 当前登录用户主键.
     */
    @Schema(description = "当前登录用户主键")
    private Long currentUserId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
