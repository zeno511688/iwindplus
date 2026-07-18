/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码返回值枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
public enum ImCodeEnum implements CommonException {

    /**
     * 无效消息指令.
     */
    INVALID_MSG_COMMAND("invalid_msg_command", "无效消息指令"),

    /**
     * 拉取用户不能超过.
     */
    PULL_USER_CANNOT_EXCEED("pull_user_cannot_exceed", "拉取用户不能超过30人"),

    /**
     * 群组人数不能超过.
     */
    GROUP_USER_CANNOT_EXCEED("group_user_cannot_exceed", "群组人数不能超过500人"),

    /**
     * 群名称已经存在.
     */
    GROUP_NAME_EXIST("group_name_exist", "群名称已经存在"),

    /**
     * 好友已经存在.
     */
    FRIEND_EXIST("friend_exist", "好友已经存在"),

    /**
     * 您不能添加自己到通讯录.
     */
    NOT_ADD_ONESELF("not_add_oneself", "您不能添加自己到通讯录"),

    /**
     * 不能自己给自己发送.
     */
    NOT_SEND_MYSELF("not_send_oneself", "不能自己给自己发送"),

    /**
     * 聊天群不存在.
     */
    CHAT_GROUP_NOT_EXIST("chat_group_not_exist", "聊天群不存在"),

    /**
     * 接收人不存在.
     */
    RECEIVER_NOT_EXIST("receiver_not_exist", "接收人不存在"),

    /**
     * 不是您的好友.
     */
    NOT_YOUR_FRIEND("not_your_friend", "不是您的好友"),
    ;

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    ImCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
