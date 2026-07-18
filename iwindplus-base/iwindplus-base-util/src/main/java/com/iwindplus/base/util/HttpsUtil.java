/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.RFC3986;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.net.url.UrlPath;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.JwtConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.util.DateUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.MDC;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;

/**
 * Http请求操作工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class HttpsUtil extends HttpUtil {

    /**
     * 获取真实ip.
     *
     * @param exchange exchange
     * @return String
     */
    public static String getRealIp(ServerWebExchange exchange) {
        final String realIp = MDC.get(HeaderConstant.REAL_IP);
        if (CharSequenceUtil.isNotBlank(realIp)) {
            return realIp;
        }

        InetSocketAddress inetSocketAddress = exchange.getRequest().getRemoteAddress();
        return Optional.ofNullable(inetSocketAddress).map(InetSocketAddress::getAddress).map(InetAddress::getHostAddress)
            .orElse(null);
    }

    /**
     * 获取真实ip.
     *
     * @param request 请求
     * @return String
     */
    public static String getRealIp(HttpServletRequest request) {
        final String realIp = MDC.get(HeaderConstant.REAL_IP);
        if (CharSequenceUtil.isNotBlank(realIp)) {
            return realIp;
        }

        return Optional.ofNullable(request).map(JakartaServletUtil::getClientIP).orElse(null);
    }

    /**
     * 设置默认国际化语言.
     *
     * @return String
     */
    public static String buildDefaultLanguage() {
        final Locale locale = Locale.getDefault();
        return new StringBuilder(locale.getLanguage())
            .append(SymbolConstant.HORIZONTAL_LINE)
            .append(locale.getCountry()).toString();
    }

    /**
     * 获取完整的请求路径，包括：域名，端口，上下文访问路径.
     *
     * @param request 请求
     * @return String
     */
    public static String getRequestContextPath(HttpServletRequest request) {
        if (null == request) {
            request = getHttpServletRequest();
        }
        if (Objects.isNull(request)) {
            return null;
        }

        return UrlBuilder.of()
            .setScheme(request.getScheme())
            .setHost(request.getServerName())
            .setPort(request.getServerPort())
            .setPath(UrlPath.of(request.getContextPath(), Charset.defaultCharset()))
            .toString();
    }

    /**
     * http获取请求头参数（全量）.
     *
     * @param request 请求
     * @return Map<String, String>
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        return getHeaders(request, null);
    }

    /**
     * http获取请求头参数（排除固定header）.
     *
     * @param request 请求
     * @return Map<String, String>
     */
    public static Map<String, String> getFilteredHeaders(HttpServletRequest request) {
        Set<String> excludeHeaders = SetUtils.union(
            Set.of(
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.CONTENT_LENGTH
            ),
            SystemConstant.TRACE_HEADERS
        );
        return getHeaders(request, excludeHeaders);
    }

    /**
     * http获取请求头参数.
     *
     * @param request        请求
     * @param excludeHeaders 排除的请求头参数（可选）
     * @return Map<String, String>
     */
    public static Map<String, String> getHeaders(
        HttpServletRequest request, Set<String> excludeHeaders) {
        Map<String, String> params = new LinkedHashMap<>(16);
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            // 排除指定Header
            if (isExcludeHeader(key, excludeHeaders)) {
                continue;
            }
            params.put(key, request.getHeader(key));
        }

        return params;
    }

    /**
     * 获取map字节大小.
     *
     * @param map 请求头map
     * @return int
     */
    public static int getHeadersSize(Map<String, String> map) {
        if (MapUtil.isEmpty(map)) {
            return 0;
        }
        return map.values().stream().filter(Objects::nonNull).mapToInt(s -> s.getBytes().length).sum();
    }

    /**
     * http获取请求头参数.
     *
     * @param request 请求
     * @return Map<String, String>
     */
    public static Map<String, String> getHeaders(HttpRequest request) {
        Map<String, String> params = new LinkedHashMap<>(16);
        final Map<String, String> headerMap = request.getHeaders().toSingleValueMap();
        params.putAll(headerMap);
        params.remove(HttpHeaders.CONTENT_TYPE);
        params.remove(HttpHeaders.CONTENT_LENGTH);
        return params;
    }

    /**
     * 从请求中，获得认证 Token
     *
     * @param request 请求
     * @return String
     */
    public static String getAuthorization(HttpRequest request) {
        final HttpHeaders headers = request.getHeaders();
        final String authorizationParam = headers.getFirst(HttpHeaders.AUTHORIZATION);
        final String wsAuthorizationParam = headers.getFirst(HeaderConstant.SEC_WEBSOCKET_PROTOCOL);
        return Optional.ofNullable(authorizationParam).orElse(wsAuthorizationParam);
    }

    /**
     * http获取请求参数.
     *
     * @param request 请求
     * @return MultiValueMap<String, String>
     */
    public static MultiValueMap<String, String> getMultiParams(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());

        parameterMap.forEach((key, values) ->
            Arrays.stream(values).forEach(value -> parameters.add(key, value))
        );
        return parameters;
    }

    /**
     * 获取参数.
     *
     * @param request 请求
     * @return Map<String, Object>
     */
    public static Map<String, Object> getRequestAndJsonParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>(16);
        Optional.ofNullable(HttpsUtil.getParams(request)).ifPresent(params::putAll);
        Optional.ofNullable(HttpsUtil.getJsonParams(request))
            .filter(CharSequenceUtil::isNotBlank)
            .map(j -> JacksonUtil.parseObject(j, new TypeReference<Map<String, Object>>() {
            })).ifPresent(params::putAll);
        return params;
    }

    /**
     * http获取请求参数.
     *
     * @param request 请求
     * @return Map<String, String>
     */
    public static Map<String, String> getParams(HttpServletRequest request) {
        return HttpsUtil.getMultiParams(request).toSingleValueMap();
    }

    /**
     * 字节数组转map（支持json或key=value形式数据）.
     *
     * @param bytes    字节数组
     * @param jsonFlag 是否 json，否则为表单
     * @return Map<String, Object>
     */
    public static Map<String, Object> getByBytes(byte[] bytes, boolean jsonFlag) {
        if (ObjectUtil.isEmpty(bytes)) {
            return new HashMap<>(16);
        }

        return getByStr(new String(bytes, StandardCharsets.UTF_8), jsonFlag);
    }

    /**
     * 字符串转map（支持json或key=value形式数据）.
     *
     * @param str      字符串
     * @param jsonFlag 是否 json，否则为表单
     * @return Map<String, Object>
     */
    public static Map<String, Object> getByStr(String str, boolean jsonFlag) {
        if (CharSequenceUtil.isBlank(str)) {
            return new HashMap<>(16);
        }

        if (jsonFlag) {
            return JacksonUtil.parseObject(str, new TypeReference<>() {
            });
        }

        return Arrays.stream(str.split(SymbolConstant.LOGICAL_AND))
            .map(pair -> pair.split(SymbolConstant.EQUAL, 2))
            .filter(keyValue -> keyValue.length == 2)
            .collect(HashMap::new, (map, keyValue) -> map.put(keyValue[0], keyValue[1]), HashMap::putAll);
    }

    /**
     * 字符串转Map（内含换行符，适用国际化文件数据解析）.
     *
     * @param content 内容
     * @return Map<String, String>
     */
    public static Map<String, String> contentToMap(String content) {
        if (CharSequenceUtil.isBlank(content)) {
            return Collections.emptyMap();
        }

        return Arrays.stream(content.split("\\r?\\n"))
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .filter(line -> !line.startsWith(SymbolConstant.WELL_NO))
            .map(line -> line.split(SymbolConstant.EQUAL, 2))
            .filter(parts -> parts.length == 2)
            .collect(Collectors.toMap(
                parts -> parts[0].trim(),
                parts -> parts[1].trim(),
                (oldVal, newVal) -> newVal
            ));
    }

    /**
     * http获取json请求参数.
     *
     * @param request 请求
     * @return String
     */
    public static String getJsonParams(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 获取请求.
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(requestAttributes) && requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                return servletRequestAttributes.getRequest();
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 获取响应.
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getHttpServletResponse() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (Objects.nonNull(requestAttributes) && requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
                return servletRequestAttributes.getResponse();
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 获取获取MDC中的数据.
     *
     * @return Map<String, String>
     */
    public static Map<String, String> getMdc() {
        final Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        if (MapUtil.isEmpty(mdcMap)) {
            return null;
        }
        mdcMap.remove(HttpHeaders.CONTENT_TYPE);
        mdcMap.remove(HttpHeaders.CONTENT_LENGTH);
        return mdcMap;
    }

    /**
     * url 参数转码.
     *
     * @param value    值
     * @param encoding 编码
     * @return String
     */
    public static String urlEncode(String value, String encoding) {
        try {
            String encoded = URLEncoder.encode(value, encoding);
            return encoded.replace("+", "%20").replace("*", "%2A").replace("~", "%7E").replace("/", "%2F");
        } catch (UnsupportedEncodingException ex) {
            log.error(ExceptionConstant.UNSUPPORTED_ENCODING_EXCEPTION, ex);
        }
        return null;
    }

    /**
     * url 参数解码.
     *
     * @param value    值
     * @param encoding 编码
     * @return String
     */
    public static String urlDecode(String value, String encoding) {
        try {
            return URLDecoder.decode(value, encoding);
        } catch (UnsupportedEncodingException ex) {
            log.error(ExceptionConstant.UNSUPPORTED_ENCODING_EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 参数转字符.
     *
     * @param params 参数
     * @return String
     */
    public static String paramToQueryString(Map<String, String> params) {
        if (ObjectUtil.isEmpty(params)) {
            return null;
        }

        return params.entrySet().stream()
            .filter(entry -> ObjectUtil.isNotEmpty(entry.getKey()) && ObjectUtil.isNotEmpty(entry.getValue()))
            .map(entry -> new StringBuilder(entry.getKey()).append(SymbolConstant.EQUAL).append(entry.getValue()).toString())
            .collect(Collectors.joining(SymbolConstant.LOGICAL_AND));
    }

    /**
     * 参数转码.
     *
     * @param param       参数
     * @param fromCharset 源字符集
     * @param toCharset   目标字符集
     */
    public static void convertParamCharset(Map<String, String> param, Charset fromCharset, Charset toCharset) {
        if (ObjectUtil.isEmpty(param)) {
            return;
        }
        param.entrySet().stream()
            .filter(entry -> ObjectUtil.isNotEmpty(entry.getKey()) && ObjectUtil.isNotEmpty(entry.getValue()))
            .forEach(entry -> entry.setValue(new String(entry.getValue().getBytes(fromCharset), toCharset)));
    }

    /**
     * 判读是否是json请求.
     *
     * @param request 请求
     * @return boolean
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.CONTENT_TYPE);
        return CharSequenceUtil.startWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 判读是否是ajax请求.
     *
     * @param request 请求
     * @return boolean
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return CharSequenceUtil.equalsIgnoreCase(HeaderConstant.XML_HTTP_REQUEST,
            request.getHeader(HeaderConstant.X_REQUESTED_WITH));
    }

    /**
     * 根据请求头获取用户代理信息.
     *
     * @param request 请求
     * @return boolean
     */
    public static UserAgent getUserAgent(HttpServletRequest request) {
        return UserAgentUtil.parse(request.getHeader(HttpHeaders.USER_AGENT));
    }

    /**
     * http响应信息输出.
     *
     * @param response       响应
     * @param httpStatusCode http状态码
     * @param data           数据
     */
    public static void responseData(
        HttpServletResponse response,
        HttpStatusCode httpStatusCode,
        Object data) {
        String resultData = (data instanceof String s) ? s
            : JacksonUtil.toJsonStr(data);

        // 设置响应头
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (Objects.nonNull(httpStatusCode)) {
            response.setStatus(httpStatusCode.value());
        }

        // 写响应体
        try (PrintWriter writer = response.getWriter()) {
            writer.write(resultData);
            writer.flush();
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
    }

    /**
     * http响应信息输出.
     *
     * @param response       响应
     * @param responseEntity 响应实体
     */
    public static void responseData(HttpServletResponse response, ResponseEntity<Object> responseEntity) {
        HttpsUtil.responseData(response, responseEntity.getStatusCode(), responseEntity.getBody());
    }

    /**
     * 获取用户信息.
     *
     * @param authorization 授权信息
     * @return UserBaseVO
     */
    public static UserBaseVO getUserInfo(String authorization) {
        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(authorization);
        } catch (ParseException ex) {
            log.error(ExceptionConstant.PARSE_EXCEPTION, ex);
        }
        if (Objects.isNull(jwsObject)) {
            return null;
        }
        final Payload payload = jwsObject.getPayload();
        final Map<String, Object> payloadMap = payload.toJSONObject();
        final Object exp = payloadMap.get(JwtConstant.EXP);
        if (exp == null) {
            return null;
        }
        final Date expDate = DateUtils.fromSecondsSinceEpoch(((Number) exp).longValue());
        if (expDate.before(new Date())) {
            return null;
        }

        return JacksonUtil.parseObject(payload.toString(), UserBaseVO.class);
    }

    /**
     * 根据采样率判断是否在范围内.
     *
     * @param sampleRate 采样率 (1-100)
     * @return boolean
     */
    public static boolean checkSampleRateInRange(Integer sampleRate) {
        if (sampleRate == null || sampleRate >= 100) {
            return true;
        }
        if (sampleRate <= 0) {
            return false;
        }
        // 每个线程独立实例，无竞争
        return ThreadLocalRandom.current().nextInt(100) < sampleRate;
    }

    /**
     * 获取字节数组.
     *
     * @param buf buf
     * @return byte[]
     */
    public static byte[] getBytes(DataBuffer buf) {
        try {
            ByteBuf byteBuf = NettyDataBufferFactory.toByteBuf(buf);
            // 内部 slice 是只读，不影引用计数
            return ByteBufUtil.getBytes(byteBuf);
        } finally {
            // Spring DataBuffer 必须手动 release，否则泄漏
            if (buf instanceof NettyDataBuffer) {
                DataBufferUtils.release(buf);
            }
        }
    }

    /**
     * 获取请求路径.
     *
     * @return String
     */
    public static String getPath() {
        final HttpServletRequest request = HttpsUtil.getHttpServletRequest();
        if (ObjectUtil.isEmpty(request)) {
            return null;
        }
        String path = request.getHeader(ApiSignConstant.X_PATH);
        if (CharSequenceUtil.isBlank(path)) {
            path = request.getServletPath();
        }
        return path;
    }

    /**
     * 字典排序拼接Map（例如："key1=value1&key2=value2"）默认开启value转义
     *
     * @param entityMap 参数
     * @return 签名串
     */
    public static String concatMap(Map<String, Object> entityMap) {
        return concatMap(entityMap, true);
    }

    /**
     * 字典排序拼接Map（例如："key1=value1&key2=value2"）.
     *
     * @param entityMap   原数据
     * @param encodeValue value是否转义
     * @return String
     */
    public static String concatMap(Map<String, Object> entityMap, boolean encodeValue) {
        if (MapUtil.isEmpty(entityMap)) {
            return CharSequenceUtil.EMPTY;
        }

        return entityMap.entrySet().stream()
            .filter(entry -> CharSequenceUtil.isNotBlank(entry.getKey()))
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                final String key = entry.getKey();
                final Object rawValue = entry.getValue();

                final String value;
                if (ObjectUtil.isNull(rawValue)) {
                    value = CharSequenceUtil.EMPTY;
                } else {
                    final String strValue = String.valueOf(rawValue);
                    value = encodeValue
                        ? RFC3986.UNRESERVED.encode(strValue, StandardCharsets.UTF_8)
                        : strValue;
                }
                return key + SymbolConstant.EQUAL + value;
            })
            .collect(Collectors.joining(SymbolConstant.LOGICAL_AND));
    }

    private static boolean isExcludeHeader(String key, Set<String> excludeHeaders) {
        if (excludeHeaders == null
            || excludeHeaders.isEmpty()) {
            return false;
        }

        return excludeHeaders.stream().anyMatch(
            header -> header.equalsIgnoreCase(key));
    }
}
