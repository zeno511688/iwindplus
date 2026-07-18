/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.util.domain.constant.UtilConstant.AddressConstant;
import com.iwindplus.base.util.domain.vo.AddressVO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 通过ip获取所在省市信息工具类.
 *
 * @author zengdegui
 * @since 2021/1/11
 */
@Slf4j
public class AddressUtil {

    private AddressUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();

    /**
     * 调用太平洋网络接口查询.
     *
     * @param ip ip（必填）
     * @return AddressVO
     */
    public static AddressVO getAddress(String ip) {
        final String url = UrlBuilder.ofHttp(AddressConstant.PCONLINE_URL_STR)
            .addQuery("json", "true")
            .addQuery("ip", ip)
            .build();
        Request request = new Request.Builder().url(url).build();
        try (Response resp = CLIENT.newCall(request).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                return null;
            }

            String body = resp.body().string();
            JsonNode data = JacksonUtil.parseTree(body);
            if (data == null) {
                return null;
            }
            return AddressVO.builder()
                .ip(ip)
                .province(data.path("pro").asText())
                .city(data.path("city").asText())
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 调用高德云图查询.
     *
     * @param appCode 应用code（必填）
     * @param ip      ip（必填）
     * @return AddressVO
     */
    public static AddressVO getAddressTwo(String appCode, String ip) {
        final String url = UrlBuilder.ofHttp(AddressConstant.GAODEYUNTU_URL_STR)
            .addQuery("ip", ip)
            .build();
        Map<String, String> headerMap = new LinkedHashMap<>(16);
        String authorization = new StringBuilder("APPCODE ").append(appCode).toString();
        headerMap.put("Authorization", authorization);

        Request request = new Request.Builder().headers(Headers.of(headerMap)).url(url).build();
        try (Response resp = CLIENT.newCall(request).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                return null;
            }
            String body = resp.body().string();
            JsonNode data = JacksonUtil.parseTree(body);
            if (data == null) {
                return null;
            }
            return AddressVO.builder()
                .ip(ip)
                .province(data.path("province").asText())
                .city(data.path("city").asText())
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 调用ip138查询.
     *
     * @param token token（必填）
     * @param ip    ip（必填）
     * @return AddressVO
     */
    public static AddressVO getAddressThree(String token, String ip) {
        final String url = UrlBuilder.ofHttp(AddressConstant.IP138_URL_STR)
            .addQuery("datatype", "jsonp")
            .addQuery("token", token)
            .addQuery("ip", ip)
            .build();
        Request request = new Request.Builder().url(url).build();
        try (Response resp = CLIENT.newCall(request).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                return null;
            }
            String body = resp.body().string();
            JsonNode obj = JacksonUtil.parseTree(body);
            if (obj == null) {
                return null;
            }
            JsonNode data = obj.get("data");
            if (data == null || !data.isArray()) {
                return null;
            }
            return AddressVO.builder()
                .ip(ip)
                .province(data.path(1).asText())
                .city(data.path(2).asText())
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    /**
     * 根据访问者的Request，返回ip、地理位置（太平洋网络）.
     *
     * @param request 请求（必填）
     * @return AddressVO
     */
    public static AddressVO getAddressByRequest(HttpServletRequest request) {
        return getAddress(JakartaServletUtil.getClientIP(request));
    }

    /**
     * 根据访问者的Request，返回ip、地理位置（高德云图）.
     *
     * @param appCode 应用code（必填）
     * @param request 请求（必填）
     * @return AddressVO
     */
    public static AddressVO getAddressTwoByRequest(String appCode, HttpServletRequest request) {
        return getAddressTwo(appCode, JakartaServletUtil.getClientIP(request));
    }

    /**
     * 根据访问者的Request，返回ip、地理位置（IP138）.
     *
     * @param token   token（必填）
     * @param request 请求（必填）
     * @return AddressVO
     */
    public static AddressVO getAddressThreeByRequest(String token, HttpServletRequest request) {
        return getAddressThree(token, JakartaServletUtil.getClientIP(request));
    }
}
