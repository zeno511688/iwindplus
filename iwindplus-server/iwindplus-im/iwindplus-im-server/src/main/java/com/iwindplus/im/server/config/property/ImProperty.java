/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.config.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 即时通讯服务配置相关属性配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "im", ignoreUnknownFields = true)
public class ImProperty {

    /**
     * 是否启用远程token校验.
     */
    @Builder.Default
    private Boolean enabledRemoteToken = Boolean.FALSE;

    /**
     * 聊天群扫码加入地址.
     */
    private String chatGroupScanUrl;

    /**
     * 邮件配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MailConfig mail = new MailConfig();

    /**
     * 短信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private SmsConfig sms = new SmsConfig();
    
    /**
     * 对象存储配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OssConfig oss = new OssConfig();

    /**
     * 邮件相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailConfig {

        /**
         * 邮件模板编码.
         */
        @Schema(description = "邮件模板编码")
        @Builder.Default
        private String tplCode = "c3f67fd355dd6098156053f68285ba3e";
    }

    /**
     * 短信相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsConfig {

        /**
         * 短信配置编码.
         */
        @Schema(description = "短信配置编码")
        @Builder.Default
        private String code = "6856e64cc228215d44c1a351454a8cc5";

        /**
         * 短信模板编码.
         */
        @Schema(description = "短信模板编码")
        @Builder.Default
        private String tplCode = "c3f67fd354dd6098154053f68285ba35";
    }

    /**
     * 对象存储相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OssConfig {

        /**
         * 对象存储模板编码.
         */
        @Schema(description = "对象存储模板编码")
        @Builder.Default
        private String tplCode = "c3f67fd355dd6098156053f68385ba35";
    }
}
