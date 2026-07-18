/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client;

import cn.hutool.core.util.StrUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.executor.impl.ApacheHttpClientExecutor;
import com.iwindplus.base.http.client.executor.impl.OkHttpClientExecutor;
import com.iwindplus.base.http.client.executor.impl.RestClientExecutor;
import com.iwindplus.base.http.client.executor.impl.WebClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.factory.ResponseExtractorStrategyFactory;
import com.iwindplus.base.http.client.filter.ApiProtectionFilter;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import com.iwindplus.base.http.client.template.HttpExecuteTemplate;
import com.iwindplus.base.http.client.template.impl.DefaultHttpExecuteTemplateImpl;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.web.domain.property.FilterProperty;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * http执行器配置.
 *
 * @author zengdegui
 * @since 2026/01/20 02:22
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({HttpClientProperty.class})
@ConditionalOnProperty(prefix = "http.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HttpClientConfiguration {

    @Resource
    private HttpClientProperty property;

    @Resource
    private DtpExecutor httpClientTaskExecutor;

    /**
     * 创建 HttpExecuteTemplate.
     *
     * @param observationExecutor    observationExecutor
     * @param circuitBreakerRegistry circuitBreakerRegistry
     * @return HttpExecuteTemplate
     */
    @Bean
    public HttpExecuteTemplate httpExecuteTemplate(
        @Autowired(required = false) ObservationExecutor observationExecutor,
        @Autowired(required = false) CircuitBreakerRegistry circuitBreakerRegistry) {
        return new DefaultHttpExecuteTemplateImpl(property, observationExecutor, circuitBreakerRegistry);
    }

    /**
     * 创建 ResponseExtractorStrategyFactory.
     *
     * @return ResponseExtractorStrategyFactory
     */
    @Bean
    public ResponseExtractorStrategyFactory responseExtractorStrategyFactory() {
        final ResponseExtractorStrategyFactory responseExtractorStrategyFactory = new ResponseExtractorStrategyFactory();
        log.info("ResponseExtractorStrategyFactory={}", responseExtractorStrategyFactory);
        return responseExtractorStrategyFactory;
    }

    /**
     * 创建 HttpClientExecutorStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return HttpClientExecutorStrategyFactory
     */
    @Bean
    public HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory(ObjectProvider<HttpClientExecutor> executorProvider) {
        return new HttpClientExecutorStrategyFactory(property, executorProvider);
    }

    /**
     * 创建 ApacheHttpClientExecutor.
     *
     * @param httpExecuteTemplate              httpExecuteTemplate
     * @param responseExtractorStrategyFactory responseExtractorStrategyFactory
     * @param closeableHttpClient              closeableHttpClient
     * @param closeableHttpAsyncClient         closeableHttpAsyncClient
     * @return ApacheHttpClientExecutor
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.apache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApacheHttpClientExecutor apacheHttpClientExecutor(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorStrategyFactory responseExtractorStrategyFactory,
        @Autowired(required = false) CloseableHttpClient closeableHttpClient,
        @Autowired(required = false) CloseableHttpAsyncClient closeableHttpAsyncClient) {
        final ApacheHttpClientExecutor apacheHttpClientExecutor = new ApacheHttpClientExecutor(property
            , httpExecuteTemplate, responseExtractorStrategyFactory
            , httpClientTaskExecutor, closeableHttpClient, closeableHttpAsyncClient);
        log.info("ApacheHttpClientExecutor={}", apacheHttpClientExecutor);
        return apacheHttpClientExecutor;
    }

    /**
     * 创建 OkHttpClientExecutor.
     *
     * @param httpExecuteTemplate       httpExecuteTemplate
     * @param responseExtractorRegistry responseExtractorRegistry
     * @param okHttpClient              okHttpClient
     * @return OkHttpClientExecutor
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.ok", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OkHttpClientExecutor okHttpClientExecutor(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorStrategyFactory responseExtractorRegistry,
        @Autowired(required = false) OkHttpClient okHttpClient) {
        final OkHttpClientExecutor okHttpClientExecutor = new OkHttpClientExecutor(property
            , httpExecuteTemplate, responseExtractorRegistry
            , httpClientTaskExecutor, okHttpClient);
        log.info("OkHttpClientExecutor={}", okHttpClientExecutor);
        return okHttpClientExecutor;
    }

    /**
     * 创建 RestClientExecutor.
     *
     * @param httpExecuteTemplate              httpExecuteTemplate
     * @param responseExtractorStrategyFactory responseExtractorStrategyFactory
     * @param loadBalancedRestClient           loadBalancedRestClient
     * @param restClient                       restClient
     * @return RestClientExecutor
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RestClientExecutor restClientExecutor(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorStrategyFactory responseExtractorStrategyFactory,
        @Autowired(required = false) @Qualifier("loadBalancedRestClient") RestClient loadBalancedRestClient,
        @Autowired(required = false) @Qualifier("restClient") RestClient restClient) {
        final RestClientExecutor restClientExecutor = new RestClientExecutor(property
            , httpExecuteTemplate, responseExtractorStrategyFactory
            , httpClientTaskExecutor, loadBalancedRestClient, restClient);
        log.info("RestClientExecutor={}", restClientExecutor);
        return restClientExecutor;
    }

    /**
     * 创建 WebClientExecutor.
     *
     * @param httpExecuteTemplate              httpExecuteTemplate
     * @param responseExtractorStrategyFactory responseExtractorStrategyFactory
     * @param loadBalancedWebClient            loadBalancedWebClient
     * @param webClient                        webClient
     * @return WebClientExecutor
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.web", name = "enabled", havingValue = "true", matchIfMissing = true)
    public WebClientExecutor webClientExecutor(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorStrategyFactory responseExtractorStrategyFactory,
        @Autowired(required = false) @Qualifier("loadBalancedWebClient") WebClient loadBalancedWebClient,
        @Autowired(required = false) @Qualifier("webClient") WebClient webClient) {
        final WebClientExecutor webClientExecutor = new WebClientExecutor(property
            , httpExecuteTemplate, responseExtractorStrategyFactory
            , httpClientTaskExecutor, loadBalancedWebClient, webClient);
        log.info("WebClientExecutor={}", webClientExecutor);
        return webClientExecutor;
    }

    /**
     * 创建 ApiProtectionProvider.
     *
     * @param filterProperty                    filterProperty
     * @param httpClientProperty                httpClientProperty
     * @param httpClientExecutorStrategyFactory httpClientExecutorStrategyFactory
     * @return ApiProtectionProvider
     */
    @Bean
    public ApiProtectionProvider apiProtectionProvider(
        @Autowired(required = false) FilterProperty filterProperty,
        HttpClientProperty httpClientProperty,
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory) {
        final ApiProtectionProvider apiProtectionProvider = new ApiProtectionProvider(filterProperty, httpClientProperty,
            httpClientExecutorStrategyFactory);
        return apiProtectionProvider;
    }

    /**
     * 创建 ApiProtectionFilter.
     *
     * @return FilterRegistrationBean<ApiProtectionFilter>
     */
    @ConditionalOnProperty(prefix = "http.client.api-protection", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean("apiProtectionFilter")
    public FilterRegistrationBean<ApiProtectionFilter> apiProtectionFilter() {
        final String beanName = StrUtil.lowerFirst(ApiProtectionFilter.class.getSimpleName());
        final FilterRegistrationBean<ApiProtectionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiProtectionFilter());
        registrationBean.addUrlPatterns(SymbolConstant.SLASH_ASTERISK);
        registrationBean.setBeanName(beanName);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        log.info("FilterRegistrationBean<ApiProtectionFilter>={}", registrationBean);
        return registrationBean;
    }
}
