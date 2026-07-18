/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.im.domain.enums.FriendStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户好友视图对象.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "用户好友视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendVO extends DbVersionBaseVO {
    /**
     * 好友状态（UN_CONFIRMED：待确认，PASSED：已通过，REJECTED：已拒绝）.
     */
    @Schema(description = "好友状态（UN_CONFIRMED：待确认，PASSED：已通过，REJECTED：已拒绝）")
    private FriendStatusEnum status;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 用户头像.
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户昵称.
     */
    @Schema(description = "用户昵称")
    private String userNickName;

    /**
     * 好友主键.
     */
    @Schema(description = "好友主键")
    private Long friendId;

    /**
     * 好友头像.
     */
    @Schema(description = "好友头像")
    private String friendAvatar;

    /**
     * 好友昵称.
     */
    @Schema(description = "好友昵称")
    private String friendNickName;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
