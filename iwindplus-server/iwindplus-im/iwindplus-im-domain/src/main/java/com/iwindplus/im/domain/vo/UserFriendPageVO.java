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
 * 用户好友分页视图对象.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "用户好友分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendPageVO extends DbVersionBaseVO {

    /**
     * 好友状态（UN_CONFIRMED：待确认，PASSED：已通过，REJECTED：已拒绝）.
     */
    @Schema(description = "好友状态（UN_CONFIRMED：待确认，PASSED：已通过，REJECTED：已拒绝）")
    private FriendStatusEnum status;

    /**
     * 用户昵称.
     */
    @Schema(description = "用户昵称")
    private String userNickName;

    /**
     * 好友昵称.
     */
    @Schema(description = "好友昵称")
    private String friendNickName;
}
