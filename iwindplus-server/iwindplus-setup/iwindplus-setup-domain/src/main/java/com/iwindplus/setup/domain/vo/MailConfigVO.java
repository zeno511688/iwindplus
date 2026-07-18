/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.vo;

import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 邮件配置视图对象.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "邮件配置视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailConfigVO extends DbVersionBaseVO {
    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 发件服务器主机.
     */
    @Schema(description = "发件服务器主机")
    private String host;

    /**
     * 发件人昵称.
     */
    @Schema(description = "发件人昵称")
    @Sensitive(type = SensitiveTypeEnum.FIRST_MASK)
    private String nickName;

    /**
     * 发件服务器账户.
     */
    @Schema(description = "发件服务器账户")
    @Sensitive(type = SensitiveTypeEnum.EMAIL)
    private String username;

    /**
     * 发件服务器密码.
     */
    @Schema(description = "发件服务器密码")
    @Sensitive(type = SensitiveTypeEnum.PASSWORD)
    private String password;

    /**
     * 发件服务器端口.
     */
    @Schema(description = "发件服务器端口")
    private Integer port;

    /**
     * 是否启用ssl（false：否，true：是）.
     */
    @Schema(description = "是否启用ssl（false：否，true：是）")
    private Boolean sslEnable;

    /**
     * 是否启用重试（false：否，true：是）.
     */
    @Schema(description = "是否启用重试（false：否，true：是）")
    private Boolean retryEnable;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
