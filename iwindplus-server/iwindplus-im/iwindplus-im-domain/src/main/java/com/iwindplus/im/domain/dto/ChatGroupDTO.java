/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.im.domain.enums.ChatGroupStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 聊天群数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "聊天群实体数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupDTO extends DbVersionBaseDTO {
    /**
     * 状态（BAN：封禁，NORMAL：正常）.
     */
    @Schema(description = "状态（BAN：封禁，NORMAL：正常）")
    private ChatGroupStatusEnum status;

    /**
     * 群名称.
     */
    @Schema(description = "群名称")
    @Length(max = 50, message = "{groupName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String groupName;

    /**
     * 群头像.
     */
    @Schema(description = "群头像")
    @Length(max = 255, message = "{avatar.length}", groups = {SaveGroup.class, EditGroup.class})
    private String groupAvatar;

    /**
     * 群二维码.
     */
    @Schema(description = "群二维码")
    @Length(max = 255, message = "{qrcode.length}", groups = {SaveGroup.class, EditGroup.class})
    private String groupQrcode;

    /**
     * 公告.
     */
    @Schema(description = "公告")
    @Length(max = 255, message = "{announcement.length}", groups = {SaveGroup.class, EditGroup.class})
    private String announcement;

    /**
     * 群限制数量.
     */
    @Schema(description = "群限制数量")
    private Integer limitNum;

    /**
     * 是否显示群成员昵称（0：否，1：是）.
     */
    @Schema(description = "是否显示群成员昵称")
    private Boolean showNickNameFlag;

    /**
     * 是否禁止修改群头像（0：否，1：是）.
     */
    @Schema(description = "是否禁止修改群头像")
    private Boolean editAvatarFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @TableField(fill = FieldFill.INSERT)
    private Long orgId;
}
