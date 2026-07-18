/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.setup.server.dal.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.base.domain.annotation.TableFieldSafe;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 视频点播配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "视频点播配置对象")
@TableName(value = "vod_config")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class VodConfigDO extends DbBaseDO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 类型.
     */
    @Schema(description = "类型")
    private VodTypeEnum type;

    /**
     * 服务器区域.
     */
    @Schema(description = "服务器区域")
    private String region;

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密匙.
     */
    @Schema(description = "密匙")
    @TableFieldSafe
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
     * 回调地址.
     */
    @Schema(description = "回调地址")
    private String notifyUrl;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @TableField(fill = FieldFill.INSERT)
    private Long orgId;
}
