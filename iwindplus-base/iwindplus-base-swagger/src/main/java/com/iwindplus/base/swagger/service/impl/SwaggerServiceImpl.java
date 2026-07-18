/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.swagger.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.RequestMethodEnum;
import com.iwindplus.base.domain.vo.AppApiVO;
import com.iwindplus.base.domain.vo.AppApiVO.ApiInfoVO;
import com.iwindplus.base.swagger.service.SwaggerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Swagger业务层接口实现类（支持多路径+多方法拆分）.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class SwaggerServiceImpl implements SwaggerService {

    @Override
    public AppApiVO getServerInfo() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap =
            SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class).getHandlerMethods();

        List<ApiInfoVO> apis = handlerMethodMap.entrySet().stream()
            .flatMap(e -> explode(e.getKey(), e.getValue()).stream())
            .filter(Objects::nonNull)
            .sorted(Comparator
                .comparing(ApiInfoVO::getControllerName, String::compareToIgnoreCase)
                .thenComparing(ApiInfoVO::getRequestMethod, String::compareToIgnoreCase)
                .thenComparing(ApiInfoVO::getApiUrl, String::compareToIgnoreCase)
                .thenComparing(ApiInfoVO::getApiName, String::compareToIgnoreCase))
            .toList();

        if (apis.isEmpty()) {
            return null;
        }

        AppApiVO result = AppApiVO
            .builder()
            .appName(SpringUtil.getApplicationName())
            .appRemark(SpringUtil.getProperty("server.servlet.application-display-name"))
            .apis(apis)
            .build();
        return result;
    }

    private List<ApiInfoVO> explode(RequestMappingInfo info, HandlerMethod hm) {
        Method method = hm.getMethod();
        Class<?> clazz = method.getDeclaringClass();

        Tag tag = clazz.getAnnotation(Tag.class);
        Operation op = method.getAnnotation(Operation.class);

        if (!isValid(tag, op)) {
            return List.of();
        }

        String controllerName = CharSequenceUtil.blankToDefault(tag.name(), tag.description());
        String apiName = CharSequenceUtil.blankToDefault(op.summary(), op.description());

        List<String> types = getRequestMethods(info.getMethodsCondition());
        List<String> urls = getApiUrls(info.getPatternsCondition());

        return types.stream()
            .flatMap(t -> urls.stream()
                .map(u -> ApiInfoVO.builder()
                    .controllerName(controllerName)
                    .className(clazz.getName())
                    .methodName(method.getName())
                    .requestMethod(t)
                    .apiName(apiName)
                    .apiUrl(u)
                    .hideFlag(Optional.ofNullable(op.hidden()).orElse(Boolean.FALSE))
                    .build()))
            .collect(Collectors.toList());
    }

    private boolean isValid(Tag tag, Operation op) {
        return null != tag && (CharSequenceUtil.isNotBlank(tag.name()) || CharSequenceUtil.isNotBlank(tag.description()))
            && null != op && (CharSequenceUtil.isNotBlank(op.summary()) || CharSequenceUtil.isNotBlank(op.description()));
    }

    private List<String> getRequestMethods(RequestMethodsRequestCondition cond) {
        Set<RequestMethod> methods = cond.getMethods();
        return CollUtil.isEmpty(methods)
            ? List.of(RequestMethodEnum.POST.getValue())
            : methods.stream().map(Enum::name).sorted().toList();
    }

    private List<String> getApiUrls(PatternsRequestCondition cond) {
        if (cond == null || CollUtil.isEmpty(cond.getPatterns())) {
            return List.of(SymbolConstant.SLASH);
        }

        return cond.getPatterns().stream()
            .map(p -> (p.startsWith(SymbolConstant.SLASH) ? p : SymbolConstant.SLASH + p))
            .map(p -> ReUtil.replaceAll(p, "/{2,}", SymbolConstant.SLASH))
            .map(p -> StrUtil.removeSuffix(p, SymbolConstant.SLASH))
            .distinct()
            .sorted()
            .toList();
    }

}