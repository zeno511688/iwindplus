/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.websocket.domain.property;

import cn.hutool.core.text.CharSequenceUtil;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Objects;

/**
 * websocket相关属性.
 *
 * @author zengdegui
 * @since 2023/11/06 20:58
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperty {

    /**
     * 服务端配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ServerConfig server = new ServerConfig();

    /**
     * ssl配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private SslConfig ssl = new SslConfig();

    /**
     * 集群配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ClusterConfig cluster = new ClusterConfig();

    public boolean useSsl() {
        return Objects.nonNull(this.ssl) && CharSequenceUtil.isNotBlank(this.ssl.keyStore) && CharSequenceUtil.isNotBlank(this.ssl.trustStore);
    }

    /**
     * 服务端配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用http.
         */
        @Builder.Default
        private Boolean httpEnabled = Boolean.FALSE;

        /**
         * 是否debug.
         */
        @Builder.Default
        private Boolean debug = Boolean.FALSE;

        /**
         * 是否线程共享配置.
         */
        @Builder.Default
        private Boolean share = Boolean.TRUE;

        /**
         * 是否用队列发送.
         */
        @Builder.Default
        private Boolean useQueueSend = Boolean.TRUE;

        /**
         * 是否用队列解码.
         */
        @Builder.Default
        private Boolean useQueueDecode = Boolean.FALSE;

        /**
         * 解码出现异常时，是否打印异常日志.
         */
        @Builder.Default
        private Boolean logWhenDecodeError = Boolean.FALSE;

        /**
         * 服务名称
         */
        @Builder.Default
        private String name = "iwindplus-websocket";

        /**
         * 服务绑定的IP地址，默认不绑定.
         */
        @Builder.Default
        private String ip = null;

        /**
         * 服务绑定的端口.
         */
        @Builder.Default
        private int port = 9326;

        /**
         * 心跳超时时间，超时会自动关闭连接 单位：秒.
         */
        @Builder.Default
        private Duration heartbeatTimeout = Duration.ofSeconds(5);

        /**
         * ip统计间隔时间.
         */
        private Long[] ipStatDurations;
    }

    /**
     * ssl配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SslConfig {

        /**
         * 密钥库存储.
         */
        private String keyStore;

        /**
         * 信任存储.
         */
        private String trustStore;

        /**
         * 密码.
         */
        private String password;
    }


    /**
     * 集群配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 发消息到组.
         */
        @Builder.Default
        private Boolean group = Boolean.TRUE;

        /**
         * 发消息给指定业务ID.
         */
        @Builder.Default
        private Boolean bsId = Boolean.TRUE;

        /**
         * 发消息给指定用户.
         */
        @Builder.Default
        private Boolean user = Boolean.TRUE;

        /**
         * 发送到指定ip.
         */
        @Builder.Default
        private Boolean ip = Boolean.TRUE;

        /**
         * 发消息给指定通道.
         */
        @Builder.Default
        private Boolean channel = Boolean.TRUE;

        /**
         * 发消息到所有连接.
         */
        @Builder.Default
        private Boolean all = Boolean.TRUE;
    }
}
