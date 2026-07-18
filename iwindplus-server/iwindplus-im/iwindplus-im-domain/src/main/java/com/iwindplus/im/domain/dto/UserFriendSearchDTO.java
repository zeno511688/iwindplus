/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.FriendStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户好友搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/09 20:20
 */
@Schema(description = "用户好友搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendSearchDTO implements Serializable {

    /**
     * 管理服务对象存储模板配置编码.
     */
    @Schema(description = "管理服务对象存储模板配置编码")
    private String mgtOssTplCode;

    /**
     * 好友状态（UN_CONFIRMED：待确认，PASSED：已通过，REJECTED：已拒绝）.
     */
    @Schema(description = "好友状态（UN_CONFIRMED：待确认，PASSED：已通过，REJECTED：已拒绝）")
    private FriendStatusEnum status;

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
