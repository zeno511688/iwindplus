/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.websocket.service;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.websocket.domain.property.WebSocketProperty;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.tio.cluster.TioClusterConfig;
import org.tio.cluster.TioClusterTopic;
import org.tio.core.intf.GroupListener;
import org.tio.core.ssl.SslConfig;
import org.tio.core.stat.IpStatListener;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.handler.HttpRequestHandler;
import org.tio.http.server.HttpServerAioListener;
import org.tio.http.server.HttpServerStarter;
import org.tio.http.server.handler.DefaultHttpRequestHandler;
import org.tio.server.ServerTioConfig;
import org.tio.utils.Threads;

/**
 * WebSocket http服务端配置.
 *
 * @author zengdegui
 * @since 2023/11/06 20:58
 */
@Slf4j
public class WebSocketHttpServerBootstrap implements SmartLifecycle {

    private AtomicBoolean started = new AtomicBoolean(false);
    private HttpRequestHandler requestHandler;
    private GroupListener groupListener;
    private IpStatListener ipStatListener;
    private HttpConfig config;
    private HttpServerStarter serverStarter;
    private WebSocketProperty property;
    private ServerTioConfig tioConfig;
    private HttpServerAioListener serverAioListener;
    private TioClusterTopic clusterTopic;

    public WebSocketHttpServerBootstrap(WebSocketProperty property) {
        this.property = property;
    }

    /**
     * 返回tio服务端配置.
     *
     * @return ServerTioConfig
     */
    public final ServerTioConfig getServerTioConfig() {
        return this.tioConfig;
    }

    @Override
    public void start() {
        if (Boolean.FALSE.equals(this.isRunning())) {
            try {
                this.doInit();
                this.doStart();
                this.started.compareAndSet(false, true);
            } catch (Exception ex) {
                log.error(ExceptionConstant.EXCEPTION, ex);
            }
        }
    }

    @Override
    public void stop() {
        boolean stop = this.serverStarter.getTioServer().stop();
        if (Boolean.TRUE.equals(stop)) {
            this.started.compareAndSet(true, false);
        }
    }

    @Override
    public boolean isRunning() {
        return this.started.get();
    }

    private void doInit() throws Exception {
        this.initConfig();
        this.initOptionalBeans();
        this.initServerStarter();
        this.initServerTioConfig();
    }

    private void doStart() throws IOException {
        WebSocketServerBootstrap.closeCheckVersion(this.serverStarter.getTioServer());
        this.serverStarter.start();
    }

    private void initConfig() {
        this.config = new HttpConfig(this.property.getServer().getPort(), false);
        this.config.setBindIp(this.property.getServer().getIp());
    }

    private void initServerStarter() {
        this.serverStarter = new HttpServerStarter(this.config, this.requestHandler, Threads.getTioExecutor(), Threads.getGroupExecutor());
    }

    private void initOptionalBeans() throws Exception {
        this.groupListener = SpringUtil.getBean(GroupListener.class);
        this.ipStatListener = SpringUtil.getBean(IpStatListener.class);
        this.serverAioListener = SpringUtil.getBean(HttpServerAioListener.class);
        this.clusterTopic = SpringUtil.getBean(TioClusterTopic.class);
        this.requestHandler = SpringUtil.getBean(HttpRequestHandler.class);
        if (Objects.isNull(this.requestHandler)) {
            this.requestHandler = new DefaultHttpRequestHandler(this.config, this.getClass());
        }
    }

    private void initServerTioConfig() throws Exception {
        this.tioConfig = this.serverStarter.getServerTioConfig();
        this.tioConfig.debug = this.property.getServer().getDebug();
        this.tioConfig.logWhenDecodeError = this.property.getServer().getLogWhenDecodeError();
        this.tioConfig.setUseQueueSend(this.property.getServer().getUseQueueSend());
        this.tioConfig.setUseQueueDecode(this.property.getServer().getUseQueueDecode());
        if (this.property.getServer().getShare()) {
            this.tioConfig.share(this.getServerTioConfig());
        }
        this.tioConfig.setName(this.property.getServer().getName());
        if (Objects.nonNull(this.property.getServer().getHeartbeatTimeout())) {
            this.tioConfig.setHeartbeatTimeout(this.property.getServer().getHeartbeatTimeout().toMillis());
        }
        if (Objects.nonNull(this.groupListener)) {
            this.tioConfig.setGroupListener(this.groupListener);
        }
        if (Objects.nonNull(this.ipStatListener)) {
            final Long[] ipStatDurations = this.property.getServer().getIpStatDurations();
            if (ipStatDurations != null) {
                this.tioConfig.ipStats.addDurations(ipStatDurations);
            }
            this.tioConfig.setIpStatListener(this.ipStatListener);
        }
        if (Objects.nonNull(this.serverAioListener)) {
            this.tioConfig.setServerAioListener(this.serverAioListener);
        }
        if (this.property.useSsl()) {
            WebSocketProperty.SslConfig ssl = this.property.getSsl();
            this.tioConfig.setSslConfig(SslConfig.forServer(ssl.getKeyStore(), ssl.getTrustStore(), ssl.getPassword()));
        }
        if (Objects.nonNull(this.clusterTopic) && Boolean.TRUE.equals(this.property.getCluster().getEnabled())) {
            TioClusterConfig clusterConfig = new TioClusterConfig(this.clusterTopic);
            clusterConfig.setCluster4all(this.property.getCluster().getAll());
            clusterConfig.setCluster4bsId(this.property.getCluster().getBsId());
            clusterConfig.setCluster4channelId(this.property.getCluster().getChannel());
            clusterConfig.setCluster4group(this.property.getCluster().getGroup());
            clusterConfig.setCluster4ip(this.property.getCluster().getIp());
            clusterConfig.setCluster4user(this.property.getCluster().getUser());
            this.tioConfig.setTioClusterConfig(clusterConfig);
        }
    }
}
