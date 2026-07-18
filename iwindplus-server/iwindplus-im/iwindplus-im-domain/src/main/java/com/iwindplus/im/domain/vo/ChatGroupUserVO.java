/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.im.domain.enums.JoinTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 聊天群用户视图对象.
 *
 * @author zengdegui
 * @since 2023/11/08 22:51
 */
@Schema(description = "聊天群用户视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupUserVO extends DbVersionBaseVO {
    /**
     * 群成员昵称.
     */
    @Schema(description = "群成员昵称")
    private String nickName;

    /**
     * 是否群主（0：否，1：是）.
     */
    @Schema(description = "是否群主")
    private Boolean leaderFlag;

    /**
     * 加入类型.
     */
    @Schema(description = "加入类型")
    private JoinTypeEnum joinType;

    /**
     * 是否同意（0：拒绝，1：同意）.
     */
    @Schema(description = "是否同意（0：拒绝，1：同意）")
    private Boolean agreeFlag;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 聊天群主键.
     */
    @Schema(description = "聊天群主键")
    private Long chatGroupId;

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
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
