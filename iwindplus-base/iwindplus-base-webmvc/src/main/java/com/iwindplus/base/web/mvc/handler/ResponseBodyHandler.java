/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.mvc.handler;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.domain.property.ResponseBodyProperty;
import com.iwindplus.base.web.support.WebManager;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应体增强处理器.
 *
 * @author zengdegui
 * @since 2021/11/4
 */
@Slf4j
@RestControllerAdvice
@EnableConfigurationProperties({ResponseBodyProperty.class})
@ConditionalOnProperty(prefix = "response.body", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResponseBodyHandler implements ResponseBodyAdvice<Object> {

    @Resource
    private WebManager webManager;

    @Resource
    private ResponseBodyProperty responseBodyProperty;

    private static final List<String> IGNORED_CLASSES = List.of(
        "SwaggerConfigResource",
        "OpenApiWebMvcResource",
        "MultipleOpenApiWebMvcResource"
    );

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = methodParameter.getMethod();
        String simpleName = method.getDeclaringClass().getSimpleName();

        List<String> ignoredClasses = this.buildIgnoredClasses();
        return !ignoredClasses.contains(simpleName) && this.responseBodyProperty.getEnabled();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass,
        ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) serverHttpResponse).getServletResponse();
        int statusCode = httpServletResponse.getStatus();

        if (Objects.nonNull(body) && Objects.nonNull(mediaType) && mediaType.includes(MediaType.APPLICATION_JSON)) {
            ResultVO<Object> result = this.buildResultVO(body, statusCode);

            // 判断是否启用响应加密
            this.webManager.encryptResult(result);
            return result;
        }

        return body;
    }

    private ResultVO<Object> buildResultVO(Object body, int statusCode) {
        if (body instanceof ResultVO resultVO) {
            return resultVO;
        }

        return HttpStatus.OK.value() == statusCode
            ? ResultVO.success(body)
            : ResultVO.error(HttpStatus.valueOf(statusCode));
    }

    private List<String> buildIgnoredClasses() {
        List<String> urls = Lists.newArrayList();
        urls.addAll(IGNORED_CLASSES);
        List<String> ignoredClasses = this.responseBodyProperty.getIgnoredClasses();
        if (CollUtil.isNotEmpty(ignoredClasses)) {
            urls.addAll(ignoredClasses);
        }
        return urls.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }
}
