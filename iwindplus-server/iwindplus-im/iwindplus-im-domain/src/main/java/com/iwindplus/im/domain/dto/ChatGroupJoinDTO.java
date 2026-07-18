/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.JoinTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 加入群聊数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/09 21:19
 */
@Schema(description = "聊天群搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupJoinDTO implements Serializable {

    /**
     * 聊天群主键.
     */
    @Schema(description = "聊天群主键")
    @NotNull(message = "{chatGroupId.notEmpty}")
    private Long chatGroupId;

    /**
     * 用户主键集合.
     */
    @Schema(description = "用户主键集合")
    @NotEmpty(message = "{userIds.notEmpty}")
    private Set<Long> userIds;

    /**
     * 加入类型.
     */
    @Schema(description = "加入类型")
    @NotNull(message = "{joinType.notEmpty}")
    private JoinTypeEnum joinType;
}
