/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 消息集成视图对象.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "消息集成视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MsgIntegrationVO implements Serializable {
    /**
     * 未读消息.
     */
    @Schema(description = "未读消息")
    private List<DirectMsgVO> unReadMsgs;

    /**
     * 未读消息条数.
     */
    @Schema(description = "未读消息条数")
    private Integer unReadMsgCount;

    /**
     * 已读消息.
     */
    @Schema(description = "已读消息")
    private List<DirectMsgVO> readMsgs;

    /**
     * 已读消息条数.
     */
    @Schema(description = "已读消息条数")
    private Integer readMsgCount;

    /**
     * 系统通知消息.
     */
    @Schema(description = "系统通知消息")
    private List<SysNoticeMsgVO> sysNoticeMsgs;

    /**
     * 系统通知消息条数.
     */
    @Schema(description = "系统通知消息条数")
    private Integer sysNoticeMsgCount;

    /**
     * 回收站.
     */
    @Schema(description = "回收站")
    private List<DirectMsgVO> recycleMsgs;

    /**
     * 回收站条数.
     */
    @Schema(description = "回收站条数")
    private Integer recycleMsgCount;
}
