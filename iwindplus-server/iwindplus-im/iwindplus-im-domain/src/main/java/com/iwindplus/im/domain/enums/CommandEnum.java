/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 指令枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum CommandEnum implements BaseEnum<String> {
    /**
     * 心跳检测（不存储）.
     */
    HEARTBEAT("heartbeat", "心跳检测"),

    /**
     * 个人通知消息（不存储）.
     */
    PERSON_NOTICE_MSG("person_notice_msg", "个人通知消息（不存储）"),

    /**
     * 直发消息（个人，存储）.
     */
    DIRECT_MSG("directMsg", "直发消息"),

    /**
     * 系统通知（存储）.
     */
    SYS_NOTICE_MSG("sysNoticeMsg", "系统通知"),

    /**
     * 好友聊天（单聊，存储）.
     */
    FRIEND_CHAT_MSG("friendChatMsg", "好友聊天（单聊）"),

    /**
     * 群聊（存储）.
     */
    GROUP_CHAT_MSG("groupChatMsg", "群聊"),

    /**
     * 群聊通知（存储）.
     */
    GROUP_CHAT_NOTICE_MSG("groupChatNoticeMsg", "群聊通知"),

    /**
     * 加好友消息（存储）.
     */
    ADD_FRIEND_MSG("addFriendMsg", "加好友消息"),

    /**
     * 离线直发消息（存储）.
     */
    OFFLINE_DIRECT_MSG("offlineDirectMsg", "离线直发消息"),

    /**
     * 离线好友聊天消息（存储）.
     */
    OFFLINE_FRIEND_CHAT_MSG("offlineFriendChatMsg", "离线好友聊天消息"),

    /**
     * 离线加好友消息（存储）.
     */
    OFFLINE_ADD_FRIEND_MSG("offlineAddFriendMsg", "离线加好友消息"),

    ;

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}
