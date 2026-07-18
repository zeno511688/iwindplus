/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.Platform;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.dto.OperateLogDTO;
import com.iwindplus.base.operate.domain.event.OperateLogEvent;
import com.iwindplus.base.operate.domain.property.OperateProperty;
import com.iwindplus.base.operate.domain.property.OperateProperty.OperateLogConfig;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.base.util.ExpressionUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * 操作日志拦截.
 *
 * @author zengdegui
 * @since 2024/4/11
 */
@Slf4j
@Aspect
@Order(100)
public class OperateLogAspect {

    @Resource
    private OperateProperty property;

    @Resource
    private KeyGenerator keyGenerator;

    @Resource
    private ApplicationEventPublisher publisher;

    /**
     * 切点.
     */
    @Pointcut("@annotation(com.iwindplus.base.operate.domain.annotation.OperateLog)")
    public void pointCutMethod() {
    }

    /**
     * 环绕通知.
     *
     * @param joinPoint 切点
     * @return Object
     * @throws Throwable 异常
     */
    @Around("pointCutMethod()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        final OperateLogConfig cfg = this.property.getLog();
        if (Boolean.FALSE.equals(this.property.getEnabled())
            || Boolean.FALSE.equals(cfg.getEnabled())) {
            return joinPoint.proceed();
        }

        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        OperateLog operateLog = method.getAnnotation(OperateLog.class);
        if (Boolean.FALSE.equals(operateLog.enabled())) {
            return joinPoint.proceed();
        }

        final String applicationName = SpringUtil.getApplicationName();

        final long beginMillis = System.currentTimeMillis();
        log.info("操作日志审计，开始时间={}", DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN));

        Object target = joinPoint.getTarget();
        String[] argNames = methodSignature.getParameterNames();
        final Object[] args = joinPoint.getArgs();
        // 执行方法
        Object result = joinPoint.proceed();
        final long endMillis = System.currentTimeMillis();
        try {
            final OperateLogDTO entity = this.buildOperateLog(applicationName, operateLog, method, argNames, args, beginMillis, target, result,
                endMillis);
            if (Optional.ofNullable(entity).map(OperateLogDTO::getBizNumber).isPresent()) {
                log.info("操作日志审计发布事件");
                this.publisher.publishEvent(new OperateLogEvent(this, entity));
            }
        } catch (Exception ex) {
            log.error("操作日志审计异常", ex);
        } finally {
            log.info("操作日志审计，应用名称={}, 结束时间={}，总执行毫秒数={}", applicationName
                , DatesUtil.parseDate(endMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endMillis - beginMillis);
        }

        return result;
    }

    private OperateLogDTO buildOperateLog(String applicationName, OperateLog entity, Method method, String[] argNames, Object[] args,
        long beginMillis, Object target, Object result, long endMillis) {
        if (shouldSkipLog(entity, method, args)) {
            return null;
        }
        final UserBaseVO userInfo = UserContextHolder.getContext();
        final Long userId = Optional.ofNullable(userInfo).map(UserBaseVO::getUserId).orElse(null);
        final Long orgId = Optional.ofNullable(userInfo).map(UserBaseVO::getOrgId).orElse(null);
        final String realName = Optional.ofNullable(userInfo).map(UserBaseVO::getRealName).orElse(null);
        final OperateLogDTO param = OperateLogDTO.builder()
            .targetServer(applicationName)
            .bizNumber(this.buildBizNumber(entity, target, method, args))
            .bizType(entity.bizType())
            .operateType(entity.operateType())
            .operateName(entity.operateName())
            .operateDesc(CharSequenceUtil.isNotBlank(entity.operateDesc()) ? entity.operateDesc() : entity.operateName())
            .responseBody(this.buildResponseBody(result))
            .requestTime(DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN))
            .responseTime(DatesUtil.parseDate(endMillis, DatePattern.NORM_DATETIME_MS_PATTERN))
            .executeTime(endMillis - beginMillis)
            .bizTraceId(MDC.get(HeaderConstant.TRACE_ID))
            .ip(MDC.get(HeaderConstant.REAL_IP))
            .userId(userId)
            .orgId(orgId)
            .createdBy(realName)
            .createdId(userId)
            .modifiedBy(userInfo.getRealName())
            .modifiedId(userId)
            .build();
        final HttpServletRequest httpServletRequest = HttpsUtil.getHttpServletRequest();
        if (httpServletRequest != null) {
            param.setRequestId(httpServletRequest.getHeader(HeaderConstant.X_REQUESTED_ID));

            if (CharSequenceUtil.isNotBlank(httpServletRequest.getContentType())
                && httpServletRequest.getContentType().contains(MediaType.APPLICATION_JSON.toString())) {
                String requestBody = this.buildRequestBody(args);
                param.setRequestBody(requestBody);
            }
        }

        final String requestParam = this.buildRequestParam(argNames, args);
        param.setRequestParam(requestParam);
        this.buildSystemInfo(param);
        return param;
    }

    private boolean shouldSkipLog(OperateLog entity, Method method, Object[] args) {
        String[] conditions = entity.conditions();
        // 没配置条件 → 不跳过
        if (ArrayUtil.isEmpty(conditions)) {
            return false;
        }
        // 解析结果
        List<Boolean> resultList;
        try {
            resultList = ExpressionUtil.parse(method, args, conditions, Boolean.class);
        } catch (Exception ex) {
            log.warn("操作日志审计条件解析异常", ex);
            return false;
        }
        if (CollUtil.isEmpty(resultList)) {
            return false;
        }
        // 任一条件为 false → 跳过日志
        return resultList.stream().anyMatch(b -> Boolean.FALSE.equals(b));
    }

    private String buildBizNumber(OperateLog entity, Object target, Method method, Object[] args) {
        try {
            String[] keys = entity.keys();
            if (ArrayUtil.isNotEmpty(keys)) {
                List<Object> resultList = ExpressionUtil.parse(method, args, keys, Object.class);
                return resultList.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(SymbolConstant.UNDERLINE));
            }
            return this.keyGenerator.generate(target, method, args).toString();
        } catch (Exception ex) {
            log.warn("操作日志审计获取业务流水号异常", ex);
            return IdUtil.simpleUUID();
        }
    }

    private Map<String, Object> buildArgsMap(String[] argNames, Object[] args) {
        Map<String, Object> argsMap = new HashMap<>(16);
        if (ArrayUtil.isNotEmpty(argNames) && ArrayUtil.isNotEmpty(args)) {
            for (int i = 0; i < argNames.length; i++) {
                argsMap.put(argNames[i], args[i]);
            }
        }
        return argsMap;
    }

    private String buildRequestBody(Object[] args) {
        if (Boolean.FALSE.equals(this.property.getLog().getEnabledRequestBody())) {
            return null;
        }
        if (ArrayUtil.isEmpty(args)) {
            return null;
        }
        return JacksonUtil.toJsonStr(args);
    }

    private String buildRequestParam(String[] argNames, Object[] args) {
        if (Boolean.FALSE.equals(this.property.getLog().getEnabledRequestParam())) {
            return null;
        }
        Map<String, Object> argsMap = this.buildArgsMap(argNames, args);
        if (MapUtil.isEmpty(argsMap)) {
            return null;
        }
        return URLUtil.buildQuery(argsMap, Charset.defaultCharset());
    }

    private String buildResponseBody(Object result) {
        if (Boolean.FALSE.equals(this.property.getLog().getEnabledResponseBody())) {
            return null;
        }
        if (Objects.isNull(result)) {
            return null;
        }
        if (Boolean.FALSE.equals(result instanceof ResultVO<?>)) {
            return result.toString();
        }

        return JacksonUtil.toJsonStr(result);
    }

    private void buildSystemInfo(OperateLogDTO entity) {
        final Map<String, String> context = HeaderContextHolder.getContext();
        Optional.ofNullable(context).map(m -> m.get(HttpHeaders.USER_AGENT)).ifPresent(userAgentStr -> {
            UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
            entity.setPlatformName(Optional.ofNullable(userAgent).map(UserAgent::getPlatform).map(Platform::getName).orElse(null));
            entity.setOsName(Optional.ofNullable(userAgent).map(UserAgent::getOs).map(OS::getName).orElse(null));
            entity.setBrowserName(Optional.ofNullable(userAgent).map(UserAgent::getBrowser).map(Browser::getName).orElse(null));
        });
    }

}
