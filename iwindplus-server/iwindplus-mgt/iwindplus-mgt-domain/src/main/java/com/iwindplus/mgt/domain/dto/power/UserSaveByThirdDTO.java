/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 第三方绑定授权注册数据传输对象.
 *
 * @author zengdegui
 * @since 2019/8/23
 */
@Schema(description = "第三方绑定授权注册数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveByThirdDTO extends UserSaveEditDTO {

    /**
     * 国家（位置）.
     */
    @Schema(description = "国家（位置）")
    @Length(max = 50, message = "{locationCountry.length}", groups = {SaveGroup.class, EditGroup.class})
    private String locationCountry;

    /**
     * 省份（位置）.
     */
    @Schema(description = "省份（位置）")
    @Length(max = 100, message = "{locationProvince.length}", groups = {SaveGroup.class, EditGroup.class})
    private String locationProvince;

    /**
     * 城市（位置）.
     */
    @Schema(description = "城市（位置）")
    @Length(max = 50, message = "{locationCity.length}", groups = {SaveGroup.class, EditGroup.class})
    private String locationCity;

    /**
     * 地区（位置）.
     */
    @Schema(description = "地区（位置）")
    @Length(max = 50, message = "{locationDistrict.length}", groups = {SaveGroup.class, EditGroup.class})
    private String locationDistrict;

    /**
     * 街道/详细地址（位置）.
     */
    @Schema(description = "街道/详细地址（位置）")
    @Length(max = 100, message = "{locationDetailAddress.length}", groups = {SaveGroup.class, EditGroup.class})
    private String locationDetailAddress;
}
