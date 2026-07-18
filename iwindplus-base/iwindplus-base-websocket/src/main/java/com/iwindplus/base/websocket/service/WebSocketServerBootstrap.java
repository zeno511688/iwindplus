/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.websocket.service;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.websocket.domain.property.WebSocketProperty;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.ReflectionUtils;
import org.tio.cluster.TioClusterConfig;
import org.tio.cluster.TioClusterTopic;
import org.tio.core.intf.GroupListener;
import org.tio.core.ssl.SslConfig;
import org.tio.core.stat.IpStatListener;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;
import org.tio.utils.Threads;
import org.tio.websocket.server.WsServerAioListener;
import org.tio.websocket.server.WsServerConfig;
import org.tio.websocket.server.WsServerStarter;
import org.tio.websocket.server.handler.IWsMsgHandler;

/**
 * WebSocket服务端配置.
 *
 * @author zengdegui
 * @since 2023/11/06 20:58
 */
@Slf4j
public class WebSocketServerBootstrap implements SmartLifecycle {

    private AtomicBoolean started = new AtomicBoolean(false);
    private IWsMsgHandler msgHandler;
    private GroupListener groupListener;
    private IpStatListener ipStatListener;
    private WsServerConfig config;
    private WsServerStarter serverStarter;
    private WebSocketProperty property;
    private ServerTioConfig tioConfig;
    private WsServerAioListener serverAioListener;
    private TioClusterTopic clusterTopic;

    public WebSocketServerBootstrap(WebSocketProperty property) {
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
        this.initRequiredBeans();
        this.initServerStarter();
        this.initServerTioConfig();
    }

    private void doStart() throws IOException {
        WebSocketServerBootstrap.closeCheckVersion(this.serverStarter.getTioServer());
        this.serverStarter.start();
    }

    private void initConfig() {
        this.config = new WsServerConfig(this.property.getServer().getPort());
        this.config.setBindIp(this.property.getServer().getIp());
    }

    private void initServerStarter() throws IOException {
        this.serverStarter = new WsServerStarter(this.config, this.msgHandler, Threads.getTioExecutor(), Threads.getGroupExecutor());
    }

    private void initOptionalBeans() {
        this.groupListener = SpringUtil.getBean(GroupListener.class);
        this.ipStatListener = SpringUtil.getBean(IpStatListener.class);
        this.serverAioListener = SpringUtil.getBean(WsServerAioListener.class);
        this.clusterTopic = SpringUtil.getBean(TioClusterTopic.class);
    }

    private void initRequiredBeans() {
        this.msgHandler = SpringUtil.getBean(IWsMsgHandler.class);
        if (Objects.isNull(this.msgHandler)) {
            throw new BizException(BizCodeEnum.NOT_FOUND_BEAN);
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

    /**
     * 用户使用时间接依赖版本，所以不做检查.
     *
     * @param server 服务
     */
    static void closeCheckVersion(TioServer server) {
        try {
            Field field = server.getClass().getDeclaredField("checkLastVersion");
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, server, false);
        } catch (NoSuchFieldException ex) {
            log.error(ExceptionConstant.NO_SUCH_FIELD_EXCEPTION, ex);
        }
    }
}
