/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.monitor.server.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Web服务配置类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Configuration
public class WebSecurityConfig {
    private final String adminContextPath;

    public WebSecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    /**
     * spring security 默认的安全策略
     *
     * @param http security注入点
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl("/");
        DefaultSecurityFilterChain chain = http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(adminContextPath + "/assets/**", adminContextPath + "/login", adminContextPath + "/actuator/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(m -> m.loginPage(adminContextPath + "/login").successHandler(successHandler))
                .logout(m -> m.logoutUrl(adminContextPath + "/logout"))
                .csrf(m -> m.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).disable())
                .build();
        log.info("SecurityFilterChain={}", chain);
        return chain;
    }

}
