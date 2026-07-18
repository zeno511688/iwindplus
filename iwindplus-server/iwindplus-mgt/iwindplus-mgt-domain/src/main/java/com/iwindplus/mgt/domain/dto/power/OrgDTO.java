/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import com.iwindplus.base.domain.validation.OtherSaveGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.mgt.domain.enums.OrgAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 组织数据传输对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "组织数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgDTO extends DbVersionBaseDTO {

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
    @Length(max = 50, message = "{code.length}", groups = {SaveGroup.class, EditGroup.class})
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class, OtherSaveGroup.class, OtherEditGroup.class})
    @Length(max = 100, message = "{name.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 简称.
     */
    @Schema(description = "简称")
    @NotBlank(message = "{abbr.notEmpty}", groups = {SaveGroup.class, EditGroup.class, OtherSaveGroup.class, OtherEditGroup.class})
    @Length(max = 50, message = "{abbr.length}", groups = {SaveGroup.class, EditGroup.class})
    private String abbr;

    /**
     * 社会信用代码.
     */
    @Schema(description = "社会信用代码")
    @Length(max = 50, message = "{uscc.length}", groups = {SaveGroup.class, EditGroup.class})
    private String uscc;

    /**
     * 企业法人.
     */
    @Schema(description = "企业法人")
    @NotBlank(message = "{legalPerson.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{legalPerson.length}", groups = {SaveGroup.class, EditGroup.class})
    private String legalPerson;

    /**
     * 负责人.
     */
    @Schema(description = "负责人")
    @Length(max = 50, message = "{leader.length}", groups = {SaveGroup.class, EditGroup.class})
    private String leader;

    /**
     * 负责人.
     */
    @Schema(description = "分管领导")
    @Length(max = 50, message = "{manager.length}", groups = {SaveGroup.class, EditGroup.class})
    private String manager;

    /**
     * logo（相对路径）.
     */
    @Schema(description = "logo（相对路径）")
    @Length(max = 255, message = "{logo.length}", groups = {SaveGroup.class, EditGroup.class})
    private String logo;

    /**
     * 营业执照.
     */
    @Schema(description = "营业执照（相对路径）")
    @NotBlank(message = "{businessLicense.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 255, message = "{businessLicense.length}", groups = {SaveGroup.class, EditGroup.class})
    private String businessLicense;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @NotBlank(message = "{mail.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{mail.length}", groups = {SaveGroup.class, EditGroup.class})
    private String mail;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @NotBlank(message = "{mobile.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{mobile.length}", groups = {SaveGroup.class, EditGroup.class})
    private String mobile;

    /**
     * 座机.
     */
    @Schema(description = "座机")
    @Length(max = 100, message = "{telephone.length}", groups = {SaveGroup.class, EditGroup.class})
    private String telephone;

    /**
     * 国家.
     */
    @Schema(description = "国家")
    @Length(max = 50, message = "{country.length}", groups = {SaveGroup.class, EditGroup.class})
    private String country;

    /**
     * 省份.
     */
    @Schema(description = "省份")
    @NotBlank(message = "{province.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{province.length}", groups = {SaveGroup.class, EditGroup.class})
    private String province;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    @NotBlank(message = "{city.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{city.length}", groups = {SaveGroup.class, EditGroup.class})
    private String city;

    /**
     * 地区.
     */
    @Schema(description = "地区")
    @NotBlank(message = "{district.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{district.length}", groups = {SaveGroup.class, EditGroup.class})
    private String district;

    /**
     * 街道/详细地址.
     */
    @Schema(description = "街道/详细地址")
    @NotBlank(message = "{detailAddress.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{detailAddress.length}", groups = {SaveGroup.class, EditGroup.class})
    private String detailAddress;

    /**
     * 官网地址.
     */
    @Schema(description = "官网地址")
    @Length(max = 255, message = "{website.length}", groups = {SaveGroup.class, EditGroup.class})
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
