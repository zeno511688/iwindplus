/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web;

import cn.hutool.core.util.StrUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.base.web.filter.RequestFilter;
import com.iwindplus.base.web.filter.XssFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 过滤器配置.
 *
 * @author zengdegui
 * @since 2023/08/31 20:32
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({FilterProperty.class})
@ConditionalOnProperty(prefix = "filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FilterConfiguration {

    /**
     * 创建 XssFilter.
     *
     * @return FilterRegistrationBean<XssFilter>
     */
    @ConditionalOnProperty(prefix = "filter.xss", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean("xssFilter")
    public FilterRegistrationBean<XssFilter> xssFilter() {
        final String beanName = StrUtil.lowerFirst(XssFilter.class.getSimpleName());
        FilterRegistrationBean<XssFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XssFilter());
        registrationBean.addUrlPatterns(SymbolConstant.SLASH_ASTERISK);
        registrationBean.setBeanName(beanName);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        log.info("FilterRegistrationBean<XssFilter>={}", registrationBean);
        return registrationBean;
    }

    /**
     * 创建 RequestFilter.
     *
     * @return FilterRegistrationBean<RequestFilter>
     */
    @ConditionalOnProperty(prefix = "filter.request", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean("requestFilter")
    public FilterRegistrationBean<RequestFilter> requestFilter() {
        final String beanName = StrUtil.lowerFirst(RequestFilter.class.getSimpleName());
        final FilterRegistrationBean<RequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestFilter());
        registrationBean.addUrlPatterns(SymbolConstant.SLASH_ASTERISK);
        registrationBean.setBeanName(beanName);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        log.info("FilterRegistrationBean<RequestFilter>={}", registrationBean);
        return registrationBean;
    }
}
