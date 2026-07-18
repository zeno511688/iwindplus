/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.vo;

import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.base.domain.enums.SmsTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 短信配置视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信配置视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsConfigVO extends DbVersionBaseVO {

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
     * 类型.
     */
    @Schema(description = "类型")
    private SmsTypeEnum type;

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密匙.
     */
    @Schema(description = "密匙")
    @Sensitive(type = SensitiveTypeEnum.CUSTOM, startInclude = 2, endReserve = 2)
    private String secretKey;

    /**
     * sts地域节点（可选）.
     */
    @Schema(description = "sts地域节点")
    private String stsEndpoint;

    /**
     * RAM角色（可选）.
     */
    @Schema(description = "RAM角色")
    private String roleArn;

    /**
     * RAM权限策略（可选，如果policy为空，则用户将获得该角色下所有权限）.
     */
    @Schema(description = "RAM权限策略")
    private String policy;

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