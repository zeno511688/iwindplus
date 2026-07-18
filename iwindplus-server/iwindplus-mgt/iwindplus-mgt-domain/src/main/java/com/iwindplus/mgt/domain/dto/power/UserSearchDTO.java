/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO extends DbPageDTO {

    /**
     * 账户是否启用.
     */
    @Schema(description = "账户是否启用")
    private Boolean enabled;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String jobNumber;

    /**
     * 用户名.
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;

    /**
     * 姓名.
     */
    @Schema(description = "姓名")
    private String realName;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    private String mail;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
