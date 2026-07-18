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
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储模板配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储板配置对象")
@TableName(value = "oss_tpl")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OssTplDO extends DbBaseDO {

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
     * 空间名.
     */
    @Schema(description = "空间名")
    private String bucketName;

    /**
     * 自定义访问域名.
     */
    @Schema(description = "自定义访问域名")
    private String accessDomain;

    /**
     * 分片上传，分片大小（单位：兆）.
     */
    @Schema(description = "分片上传，分片大小（单位：兆）")
    private Long partSize;

    /**
     * 是否启用断点上传（false：否，true：是）.
     */
    @Schema(description = "是否启用断点上传（false：否，true：是）")
    private Boolean broke;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 配置主键.
     */
    @Schema(description = "配置主键")
    private Long configId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @TableField(fill = FieldFill.INSERT)
    private Long orgId;
}