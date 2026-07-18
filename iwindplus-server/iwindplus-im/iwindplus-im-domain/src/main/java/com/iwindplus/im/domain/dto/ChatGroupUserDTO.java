/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.im.domain.enums.JoinTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 聊天群用户数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/08 22:51
 */
@Schema(description = "聊天群用户数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupUserDTO extends DbVersionBaseDTO {
    /**
     * 群成员昵称.
     */
    @Schema(description = "群成员昵称")
    @Length(max = 50, message = "{nickName.length}", groups = {SaveGroup.class, EditGroup.class})
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
    @NotNull(message = "{chatGroupId.notEmpty}", groups = {EditGroup.class})
    private Long chatGroupId;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    @NotNull(message = "{userId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
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
    @TableField(fill = FieldFill.INSERT)
    private Long orgId;
}
