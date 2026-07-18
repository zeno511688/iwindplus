/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import com.iwindplus.base.domain.vo.DbBaseTwoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 短信验证码日志视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信验证码日志视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCaptchaLogPageVO extends DbBaseTwoVO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 短信模板编码.
     */
    @Schema(description = "短信模板编码")
    private String tplCode;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;

    /**
     * 验证码.
     */
    @Schema(description = "验证码")
    private String captcha;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 是否使用（false：未使用，true：已使用）
     */
    @Schema(description = "是否使用（false：未使用，true：已使用）")
    private Boolean used;

    /**
     * 使用时间.
     */
    @Schema(description = "使用时间")
    private LocalDateTime useTime;
}