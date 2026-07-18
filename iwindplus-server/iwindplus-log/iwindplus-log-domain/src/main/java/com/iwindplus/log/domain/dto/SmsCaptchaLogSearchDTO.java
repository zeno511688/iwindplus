/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 短信验证码日志搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信验证码日志搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCaptchaLogSearchDTO extends DbPageDTO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Length(max = 100, message = "{bizNumber.length}")
    private String bizNumber;

    /**
     * 短信模板编码.
     */
    @Schema(description = "短信模板编码")
    @Length(max = 100, message = "{tplCode.length}")
    private String tplCode;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 工号
     */
    @Schema(description = "工号")
    @Length(max = 100, message = "{jobNumber.length}")
    private String jobNumber;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @Length(max = 100, message = "{mobile.length}")
    private String mobile;
}
