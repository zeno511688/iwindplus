/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.config;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.auth.server.config.property.AuthProperty;
import com.iwindplus.auth.server.handler.CustomLogoutHandler;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.web.support.WebManager;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 服务安全相关配置
 *
 * @author zengdegui
 * @since 2020/3/24
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({AuthProperty.class})
public class WebSecurityConfiguration {

    @Resource
    private AuthProperty authProperty;

    @Resource
    private WebEndpointProperties webEndpointProperties;

    @Resource
    private WebManager webManager;

    @Resource
    private ApplicationEventPublisher publisher;

    /**
     * defaultSecurityFilterChain.
     *
     * @param http                 http
     * @param authorizationService authorizationService
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain defaultSecurityFilterChain(
        HttpSecurity http,
        OAuth2AuthorizationService authorizationService) throws Exception {
        List<String> ignoredPatterns = this.getIgnoredPatterns();
        http.authorizeHttpRequests(requests -> {
                requests.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
                if (CollUtil.isNotEmpty(ignoredPatterns)) {
                    requests.requestMatchers(ignoredPatterns.toArray(new String[0])).permitAll();
                }

                requests.anyRequest().authenticated();
            })
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(login -> login
                // 指定登录页面
                .loginPage(AuthConstant.LOGIN_URL)
                // 指定表单提交接口
                .loginProcessingUrl(AuthConstant.LOGIN_SUBMIT_URL)
            )
            .logout(logout -> logout
                .addLogoutHandler(
                    new CustomLogoutHandler(authorizationService, this.webManager, this.authProperty, this.publisher))
            );
        return http.build();
    }

    @Nullable
    private List<String> getIgnoredPatterns() {
        List<String> urls = Lists.newArrayList();
        urls.add(AuthConstant.LOGIN_URL);
        urls.add(AuthConstant.LOGIN_SUBMIT_URL);
        urls.add(AuthConstant.LOGOUT_URL);
        urls.add(AuthConstant.CONSENT_URL);
        urls.add(CommonConstant.SymbolConstant.BASE_PATH);
        List<String> ignoredPatterns = this.authProperty.getIgnoredPatterns();
        if (CollUtil.isNotEmpty(ignoredPatterns)) {
            urls.addAll(ignoredPatterns);
        }
        final String basePath = this.webEndpointProperties.getBasePath();
        final Set<String> includeList = this.webEndpointProperties.getExposure().getInclude();
        if (CollUtil.isNotEmpty(includeList)) {
            final List<String> urlList = includeList.stream()
                .filter(Objects::nonNull).map(m ->
                    new StringBuilder(basePath)
                        .append(CommonConstant.SymbolConstant.SLASH)
                        .append(m)
                        .append(CommonConstant.SymbolConstant.BASE_PATH).toString()
                ).toList();
            if (CollUtil.isNotEmpty(urlList)) {
                urls.addAll(urlList);
            }
        }
        return urls;
    }
}
