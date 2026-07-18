/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.base.web.domain.property.FilterProperty.XssFilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * XSS 请求包装器.
 *
 * @author zengdegui
 * @since 2026/02/01
 */
@Slf4j
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final char XSS_CHAR_LT = '<';
    private static final char XSS_CHAR_AMP = '&';

    private static final Set<String> XSS_DANGEROUS_KEYWORDS = Set.of(
        "javascript:",
        "onerror",
        "onload"
    );

    private final XssFilterConfig cfg;

    /**
     * 请求级别缓存
     */
    private final boolean skipXss;
    private final Set<String> ignoredSymbolSet;

    public XssHttpServletRequestWrapper(HttpServletRequest request, FilterProperty property) {
        super(request);
        this.cfg = property.getXss();

        // 是否跳过 XSS（只算一次）
        this.skipXss = computeSkipXss(request);

        // ignoredSymbol 预处理成 Set
        this.ignoredSymbolSet = CollUtil.isNotEmpty(cfg.getIgnoredSymbol())
            ? new HashSet<>(cfg.getIgnoredSymbol())
            : Collections.emptySet();
    }

    @Override
    public String getQueryString() {
        return clean(super.getQueryString());
    }

    @Override
    public String getParameter(String name) {
        return clean(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (ArrayUtil.isEmpty(values)) {
            return new String[0];
        }

        List<String> result = new ArrayList<>(values.length);
        for (String v : values) {
            if (v != null) {
                result.add(clean(v));
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        if (MapUtil.isEmpty(map)) {
            return Map.of();
        }

        Map<String, String[]> result = new HashMap<>(map.size());
        for (Map.Entry<String, String[]> e : map.entrySet()) {
            if (CharSequenceUtil.isBlank(e.getKey()) || ArrayUtil.isEmpty(e.getValue())) {
                continue;
            }
            result.put(e.getKey(), getParameterValues(e.getKey()));
        }
        return result;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return cleanEnumeration(super.getParameterNames());
    }

    @Override
    public Object getAttribute(String name) {
        return cleanValue(super.getAttribute(name));
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return cleanEnumeration(super.getAttributeNames());
    }

    @Override
    public String getHeader(String name) {
        return clean(super.getHeader(name));
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return cleanEnumeration(super.getHeaders(name));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isJsonRequest()) {
            return super.getInputStream();
        }

        String json = readJson(super.getInputStream());
        if (CharSequenceUtil.isBlank(json)) {
            return wrapInputStream(new byte[0]);
        }

        // JSON 不可能有 XSS，直接跳过
        if (!maybeContainsXss(json) || skipXss) {
            return wrapInputStream(json.getBytes(StandardCharsets.UTF_8));
        }

        byte[] cleaned = cleanJson(json);
        return wrapInputStream(cleaned);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(
            new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)
        );
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        // 空字符串直接放行（没有 XSS 风险）
        if (value.isEmpty()) {
            return value;
        }

        if (skipXss || !maybeContainsXss(value)) {
            return value;
        }

        for (String symbol : ignoredSymbolSet) {
            if (value.contains(symbol)) {
                return value;
            }
        }

        return JacksonUtil.cleanByJsoup(value, cfg.getTagWhiteList());
    }

    private Object cleanValue(Object value) {
        if (value instanceof String s) {
            return clean(s);
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new HashMap<>(map.size());
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    result.put(e.getKey(), cleanValue(e.getValue()));
                }
            }
            return result;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o != null) {
                    result.add(cleanValue(o));
                }
            }
            return result;
        }
        return value;
    }

    private byte[] cleanJson(String json) {
        try {
            JsonNode node = JacksonUtil.parseTree(json);
            if (node == null) {
                return json.getBytes(StandardCharsets.UTF_8);
            }
            JacksonUtil.cleanJsonNode(node, this::clean);
            return JacksonUtil.toJsonBytes(node);
        } catch (Exception e) {
            log.error(ExceptionConstant.IO_EXCEPTION, e);
            return json.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String readJson(ServletInputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(CharSequenceUtil.trim(line));
            }
        } catch (IOException e) {
            log.error(ExceptionConstant.IO_EXCEPTION, e);
        }
        return sb.toString();
    }

    private boolean computeSkipXss(HttpServletRequest request) {
        if (Boolean.FALSE.equals(cfg.getEnabledSkip())) {
            return false;
        }
        List<String> ignoredApi = cfg.getIgnoredApi();
        return CollUtil.isNotEmpty(ignoredApi)
            && PathMatchUtil.match(ignoredApi, request.getRequestURI());
    }

    private boolean maybeContainsXss(String value) {
        if (value.indexOf(XSS_CHAR_LT) >= 0 || value.indexOf(XSS_CHAR_AMP) >= 0) {
            return true;
        }

        String lower = value.toLowerCase();
        for (String keyword : XSS_DANGEROUS_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isJsonRequest() {
        String ct = getHeader(HttpHeaders.CONTENT_TYPE);
        return CharSequenceUtil.isNotBlank(ct)
            && ct.toLowerCase().startsWith(MediaType.APPLICATION_JSON_VALUE);
    }

    private ServletInputStream wrapInputStream(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return bis.read();
            }
        };
    }

    private Enumeration<String> cleanEnumeration(Enumeration<String> src) {
        return new Enumeration<>() {
            String next;

            @Override
            public boolean hasMoreElements() {
                while (next == null && src.hasMoreElements()) {
                    String v = src.nextElement();
                    if (CharSequenceUtil.isNotBlank(v)) {
                        next = clean(v);
                    }
                }
                return next != null;
            }

            @Override
            public String nextElement() {
                if (!hasMoreElements()) {
                    return null;
                }
                String r = next;
                next = null;
                return r;
            }
        };
    }
}