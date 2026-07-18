/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.mgt.domain.enums.OrgAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织详情视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "组织详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgVO extends DbVersionBaseVO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）.
     */
    @Schema(description = "审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）")
    private OrgAuditStatusEnum auditStatus;

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
     * 简称.
     */
    @Schema(description = "简称")
    private String abbr;

    /**
     * 社会信用代码.
     */
    @Schema(description = "社会信用代码")
    private String uscc;

    /**
     * 企业法人.
     */
    @Schema(description = "企业法人")
    @Sensitive(type = SensitiveTypeEnum.FIRST_MASK)
    private String legalPerson;

    /**
     * 负责人.
     */
    @Schema(description = "负责人")
    @Sensitive(type = SensitiveTypeEnum.FIRST_MASK)
    private String leader;

    /**
     * 负责人.
     */
    @Schema(description = "分管领导")
    @Sensitive(type = SensitiveTypeEnum.FIRST_MASK)
    private String manager;

    /**
     * logo（相对路径）.
     */
    @Schema(description = "logo（相对路径）")
    private String logo;

    /**
     * 营业执照.
     */
    @Schema(description = "营业执照（相对路径）")
    private String businessLicense;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @Sensitive(type = SensitiveTypeEnum.EMAIL)
    private String mail;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @Sensitive(type = SensitiveTypeEnum.MOBILE_PHONE)
    private String mobile;

    /**
     * 座机.
     */
    @Schema(description = "座机")
    @Sensitive(type = SensitiveTypeEnum.FIXED_PHONE)
    private String telephone;

    /**
     * 国家.
     */
    @Schema(description = "国家")
    private String country;

    /**
     * 省份.
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 地区.
     */
    @Schema(description = "地区")
    private String district;

    /**
     * 街道/详细地址.
     */
    @Schema(description = "街道/详细地址")
    private String detailAddress;

    /**
     * 官网地址.
     */
    @Schema(description = "官网地址")
    private String website;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;
}
