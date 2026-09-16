/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client;

import cn.hutool.core.util.StrUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.http.client.domain.constant.HttpClientConstant;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.base.http.client.support.impl.ApacheHttpClientExecuteHandler;
import com.iwindplus.base.http.client.support.impl.OkHttpClientExecuteHandler;
import com.iwindplus.base.http.client.support.impl.RestClientExecuteHandler;
import com.iwindplus.base.http.client.support.impl.WebClientExecuteHandler;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.factory.ResponseExtractorFactory;
import com.iwindplus.base.http.client.filter.ApiProtectionFilter;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import com.iwindplus.base.http.client.support.HttpExecuteTemplate;
import com.iwindplus.base.http.client.support.impl.DefaultHttpExecuteTemplateImpl;
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
public class HttpClientConfiguration {

    @Resource
    private HttpClientProperty property;

    @Resource(name = HttpClientConstant.THREAD_POOL_BEAN_NAME)
    private DtpExecutor threadPoolExecutor;

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
     * 创建 ResponseExtractorFactory.
     *
     * @return ResponseExtractorFactory
     */
    @Bean
    public ResponseExtractorFactory responseExtractorFactory() {
        final ResponseExtractorFactory responseExtractorFactory = new ResponseExtractorFactory();
        log.info("ResponseExtractorFactory={}", responseExtractorFactory);
        return responseExtractorFactory;
    }

    /**
     * 创建 httpClientExecuteHandlerFactory.
     *
     * @param executorProvider 执行器提供者
     * @return httpClientExecuteHandlerFactory
     */
    @Bean
    public HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory(ObjectProvider<HttpClientExecuteHandler> executorProvider) {
        return new HttpClientExecuteHandlerFactory(property, executorProvider);
    }

    /**
     * 创建 ApacheHttpClientExecuteHandler.
     *
     * @param httpExecuteTemplate      httpExecuteTemplate
     * @param responseExtractorFactory responseExtractorFactory
     * @param closeableHttpClient      closeableHttpClient
     * @param closeableHttpAsyncClient closeableHttpAsyncClient
     * @return ApacheHttpClientExecuteHandler
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.apache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ApacheHttpClientExecuteHandler apacheHttpClientExecuteHandler(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorFactory responseExtractorFactory,
        @Autowired(required = false) CloseableHttpClient closeableHttpClient,
        @Autowired(required = false) CloseableHttpAsyncClient closeableHttpAsyncClient) {
        final ApacheHttpClientExecuteHandler apacheHttpClientExecuteHandler = new ApacheHttpClientExecuteHandler(property
            , httpExecuteTemplate, responseExtractorFactory
            , threadPoolExecutor, closeableHttpClient, closeableHttpAsyncClient);
        log.info("ApacheHttpClientExecuteHandler={}", apacheHttpClientExecuteHandler);
        return apacheHttpClientExecuteHandler;
    }

    /**
     * 创建 OkHttpClientExecuteHandler.
     *
     * @param httpExecuteTemplate      httpExecuteTemplate
     * @param responseExtractorFactory responseExtractorFactory
     * @param okHttpClient             okHttpClient
     * @return OkHttpClientExecuteHandler
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.ok", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OkHttpClientExecuteHandler okHttpClientExecuteHandler(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorFactory responseExtractorFactory,
        @Autowired(required = false) OkHttpClient okHttpClient) {
        final OkHttpClientExecuteHandler okHttpClientExecuteHandler = new OkHttpClientExecuteHandler(property
            , httpExecuteTemplate, responseExtractorFactory
            , threadPoolExecutor, okHttpClient);
        log.info("OkHttpClientExecuteHandler={}", okHttpClientExecuteHandler);
        return okHttpClientExecuteHandler;
    }

    /**
     * 创建 RestClientExecuteHandler.
     *
     * @param httpExecuteTemplate      httpExecuteTemplate
     * @param responseExtractorFactory responseExtractorFactory
     * @param loadBalancedRestClient   loadBalancedRestClient
     * @param restClient               restClient
     * @return RestClientExecuteHandler
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RestClientExecuteHandler restClientExecuteHandler(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorFactory responseExtractorFactory,
        @Autowired(required = false) @Qualifier("loadBalancedRestClient") RestClient loadBalancedRestClient,
        @Autowired(required = false) @Qualifier("restClient") RestClient restClient) {
        final RestClientExecuteHandler restClientExecuteHandler = new RestClientExecuteHandler(property
            , httpExecuteTemplate, responseExtractorFactory
            , threadPoolExecutor, loadBalancedRestClient, restClient);
        log.info("RestClientExecuteHandler={}", restClientExecuteHandler);
        return restClientExecuteHandler;
    }

    /**
     * 创建 WebClientExecuteHandler.
     *
     * @param httpExecuteTemplate      httpExecuteTemplate
     * @param responseExtractorFactory responseExtractorFactory
     * @param loadBalancedWebClient    loadBalancedWebClient
     * @param webClient                webClient
     * @return WebClientExecuteHandler
     */
    @Bean
    @ConditionalOnProperty(prefix = "http.client.web", name = "enabled", havingValue = "true", matchIfMissing = true)
    public WebClientExecuteHandler webClientExecuteHandler(
        HttpExecuteTemplate httpExecuteTemplate,
        ResponseExtractorFactory responseExtractorFactory,
        @Autowired(required = false) @Qualifier("loadBalancedWebClient") WebClient loadBalancedWebClient,
        @Autowired(required = false) @Qualifier("webClient") WebClient webClient) {
        final WebClientExecuteHandler webClientExecuteHandler = new WebClientExecuteHandler(property
            , httpExecuteTemplate, responseExtractorFactory
            , threadPoolExecutor, loadBalancedWebClient, webClient);
        log.info("WebClientExecuteHandler={}", webClientExecuteHandler);
        return webClientExecuteHandler;
    }

    /**
     * 创建 ApiProtectionProvider.
     *
     * @param filterProperty           filterProperty
     * @param httpClientProperty       httpClientProperty
     * @param httpClientExecuteHandlerFactory httpClientExecuteHandlerFactory
     * @return ApiProtectionProvider
     */
    @Bean
    public ApiProtectionProvider apiProtectionProvider(
        @Autowired(required = false) FilterProperty filterProperty,
        HttpClientProperty httpClientProperty,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {
        final ApiProtectionProvider apiProtectionProvider = new ApiProtectionProvider(filterProperty, httpClientProperty,
            httpClientExecuteHandlerFactory);
        return apiProtectionProvider;
    }

    /**
     * 创建 ApiProtectionFilter.
     *
     * @param apiProtectionProvider API防护提供者
     * @return FilterRegistrationBean<ApiProtectionFilter>
     */
    @Bean("apiProtectionFilter")
    public FilterRegistrationBean<ApiProtectionFilter> apiProtectionFilter(ApiProtectionProvider apiProtectionProvider) {
        final String beanName = StrUtil.lowerFirst(ApiProtectionFilter.class.getSimpleName());
        final FilterRegistrationBean<ApiProtectionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiProtectionFilter(apiProtectionProvider));
        registrationBean.addUrlPatterns(SymbolConstant.SLASH_ASTERISK);
        registrationBean.setBeanName(beanName);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        log.info("FilterRegistrationBean<ApiProtectionFilter>={}", registrationBean);
        return registrationBean;
    }
}
