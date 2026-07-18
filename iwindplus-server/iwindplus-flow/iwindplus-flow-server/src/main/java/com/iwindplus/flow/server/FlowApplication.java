/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 工程启动入口.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@RefreshScope
@EnableAsync
@EnableDynamicTp
public class FlowApplication {

    public static void main(String[] args) throws UnknownHostException {
        System.setProperty("nacos.logging.default.config.enabled", "false");
        ConfigurableApplicationContext application = SpringApplication.run(FlowApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = Optional.ofNullable(env.getProperty("server.servlet.context-path")).orElse("");
        String applicationDisplayName = Optional.ofNullable(env.getProperty("server.servlet.application-display-name")).orElse("");
        log.info("\n----------------------------------------------------------\n\t" +
            applicationDisplayName + "正在运行! 访问地址如下: \n\t" +
            "本地访问地址：http://localhost:" + port + path + "\n\t" +
            "外部访问地址：http://" + ip + ":" + port + path + "\n\t" +
            "本地文档地址：http://localhost:" + port + path + "/doc.html" + "\n\t" +
            "外部文档地址：http://" + ip + ":" + port + path + "/doc.html" +
            "\n----------------------------------------------------------\n");
    }
}
