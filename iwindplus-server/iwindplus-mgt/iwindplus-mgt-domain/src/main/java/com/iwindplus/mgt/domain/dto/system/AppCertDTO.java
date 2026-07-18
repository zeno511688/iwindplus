/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 应用凭证数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "应用凭证数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppCertDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{name.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String secretKey;

    /**
     * 超时时间.
     */
    @Schema(description = "超时时间（单位：秒）")
    private Integer timeout;

    /**
     * 应用凭证类型.
     */
    @Schema(description = "应用凭证类型（API_SIGN：API签名，SERVICE_SIGN：服务签名）")
    @NotNull(message = "{certType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private AppCertTypeEnum certType;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

}
