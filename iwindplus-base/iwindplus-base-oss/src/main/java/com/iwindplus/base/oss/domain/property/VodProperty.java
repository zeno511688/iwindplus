/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.property;

import com.iwindplus.base.domain.dto.AkSkDTO;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 视频点播相关属性.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "vod")
public class VodProperty {

    /**
     * 阿里云视频点播配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 阿里云视频点播相关属性.
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
         * 服务器区域（必填）.
         */
        private String region;

        /**
         * sts配置（可选）.
         */
        @NestedConfigurationProperty
        private StsTokenDTO sts;
    }
}
