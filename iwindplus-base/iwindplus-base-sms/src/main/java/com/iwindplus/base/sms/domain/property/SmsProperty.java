/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.property;

import com.iwindplus.base.domain.dto.AkSkDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.LocalDateTime;

/**
 * 阿里云短信相关属性.
 *
 * @author zengdegui
 * @since 2023/6/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "sms")
public class SmsProperty {

    /**
     * 阿里云短信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 七牛云短信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private QiniuConfig qiniu = new QiniuConfig();

    /**
     * 凌凯短信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private LingkaiConfig lingkai = new LingkaiConfig();

    /**
     * 麦讯通短信配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MxtongConfig mxtong = new MxtongConfig();

    /**
     * 短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AliyunConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 签名名称（必填）.
         */
        private String signName;

        /**
         * 模板内容（必填）.
         */
        private String templateContent;

        /**
         * sts配置（可选）.
         */
        @NestedConfigurationProperty
        private StsConfig sts;

        /**
         * sts相关属性.
         *
         * @author zengdegui
         * @since 2023/6/1
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StsConfig {

            /**
             * sts地域节点（必填，如：sts.cn-shenzhen.aliyuncs.com）.
             */
            private String endpoint;

            /**
             * RAM角色（必填）.
             */
            private String roleArn;

            /**
             * RAM权限策略（可选）.
             */
            private String policy;

            /**
             * 访问key（可选，会自动生成）.
             */
            private String accessKey;

            /**
             * 密钥（可选，会自动生成）.
             */
            private String secretKey;

            /**
             * 上传授权安全令牌（可选，会自动生成）.
             */
            private String securityToken;

            /**
             * 安全令牌过期时间（可选，会自动生成）
             */
            private LocalDateTime expiration;
        }
    }

    /**
     * 七牛云短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QiniuConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 模板内容（必填）.
         */
        private String templateContent;
    }

    /**
     * 凌凯短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LingkaiConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 模板内容（必填）.
         */
        private String templateContent;
    }

    /**
     * 麦讯通短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MxtongConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 模板内容（必填）.
         */
        private String templateContent;
    }
}

