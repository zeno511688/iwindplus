/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信小程序二维码数据传输对象.
 *
 * @author zengdegui
 * @since 2021/9/2
 */
@Schema(description = "微信小程序二维码数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WechatMaGetQrCodeDTO implements Serializable {
    /**
     * 小程序的appId.
     */
    @Schema(description = "小程序的appId")
    @NotBlank(message = "{accessKey.notEmpty}")
    private String accessKey;

    /**
     * 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~， 其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）.
     */
    @Schema(description = "小程序的scene")
    private String scene;

    /**
     * 必须是已经发布的小程序页面，例如 "pages/index/index" ,如果不填写这个字段，默认跳主页面.
     */
    @Schema(description = "必须是已经发布的小程序页面")
    private String page;

    /**
     * 默认true 检查 page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）； 为 false 时允许小程序未发布或者 page 不存在，但 page 有数量上限（60000个）请勿滥用.
     */
    @Schema(description = "检查page是否存在")
    private Boolean checkPath;

    /**
     * 默认"release" 要打开的小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop".
     */
    @Schema(description = "小程序版本")
    private String envVersion;

    /**
     * 默认430 二维码的宽度.
     */
    @Schema(description = "二维码的宽度")
    private Integer width;

    /**
     * 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码.
     */
    @Schema(description = "是否需要透明底色")
    private Boolean isHyaline;
}
