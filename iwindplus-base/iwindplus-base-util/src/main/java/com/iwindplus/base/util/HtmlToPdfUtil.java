/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.net.NetUtil;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * html转pdf工具类.
 *
 * @author zengdegui
 * @since 2025/11/21 00:42
 */
@Slf4j
public class HtmlToPdfUtil {

    private HtmlToPdfUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 允许的协议.
     */
    private static final Set<String> ALLOWED_PROTOCOLS = new HashSet<>(List.of("https", "http"));

    /**
     * 连接超时时间（毫秒）.
     */
    private static final int CONNECT_TIMEOUT = 5000;

    /**
     * 读取超时时间（毫秒）.
     */
    private static final int READ_TIMEOUT = 30000;

    /**
     * url转pdf字节.
     *
     * @param url 路径
     */
    public static byte[] toPdfBytesByUrl(String url) {
        validateUrl(url);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out, new WriterProperties());
            InputStream inputStream = createSafeConnection(url)) {
            HtmlConverter.convertToPdf(inputStream, writer);
            return out.toByteArray();
        } catch (IOException ex) {
            log.error("toPdfBytesByUrl 失败", ex);
            throw new BizException(BizCodeEnum.URL_TO_PDF_BYTES_ERROR);
        }
    }

    /**
     * html转pdf，并下载（获取链接方式）.
     *
     * @param url      路径
     * @param fileName pdf文件名
     * @param response 响应
     * @return Pattern
     */
    public static void toPdfByUrl(String url, String fileName, HttpServletResponse response) {
        validateUrl(url);
        FilesUtil.setHttpServletResponse(fileName, response);
        try (InputStream inputStream = createSafeConnection(url);
            OutputStream out = response.getOutputStream()) {
            HtmlConverter.convertToPdf(inputStream, out);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
            throw new BizException(BizCodeEnum.URL_TO_PDF_DOWNLOAD_ERROR);
        }
    }

    /**
     * html转pdf字节.
     *
     * @param htmlContent html内容
     */
    public static byte[] toPdfBytesByHtml(String htmlContent) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out, new WriterProperties())) {
            HtmlConverter.convertToPdf(htmlContent, writer);
            return out.toByteArray();
        } catch (IOException ex) {
            log.error("toPdfBytesByHtml 失败", ex);
            throw new BizException(BizCodeEnum.HTML_TO_PDF_BYTES_ERROR);
        }
    }

    /**
     * html转pdf，并下载（html内容方式）.
     *
     * @param htmlContent html内容
     * @param fileName    pdf文件名
     * @param response    响应
     * @return Pattern
     */
    public static void toPdfByHtml(String htmlContent, String fileName, HttpServletResponse response) {
        FilesUtil.setHttpServletResponse(fileName, response);
        try (OutputStream out = response.getOutputStream()) {
            HtmlConverter.convertToPdf(htmlContent, out);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
            throw new BizException(BizCodeEnum.HTML_TO_PDF_DOWNLOAD_ERROR);
        }
    }

    /**
     * 验证URL安全性.
     *
     * @param url URL
     */
    private static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new BizException(BizCodeEnum.INVALID_URL);
        }

        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol().toLowerCase();

            // 检查协议
            if (!ALLOWED_PROTOCOLS.contains(protocol)) {
                log.warn("Invalid protocol: {}", protocol);
                throw new BizException(BizCodeEnum.INVALID_URL_PROTOCOL);
            }

            // 检查主机地址
            String host = parsedUrl.getHost();
            validateHost(host);
        } catch (IOException ex) {
            log.error("Invalid URL: {}", url, ex);
            throw new BizException(BizCodeEnum.INVALID_URL);
        }
    }

    /**
     * 验证主机地址安全性.
     *
     * @param host 主机地址
     */
    private static void validateHost(String host) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                String ip = addr.getHostAddress();

                // 使用 Hutool 的 NetUtil.isInnerIP() 判断是否为内网IP
                if (NetUtil.isInnerIP(ip)) {
                    log.warn("Private IP access blocked: {}", ip);
                    throw new BizException(BizCodeEnum.PRIVATE_IP_ACCESS_DENIED);
                }

                // 检查是否为回环地址
                if (addr.isLoopbackAddress()) {
                    log.warn("Loopback address access blocked: {}", ip);
                    throw new BizException(BizCodeEnum.LOOPBACK_ADDRESS_ACCESS_DENIED);
                }

                // 检查是否为链路本地地址
                if (addr.isLinkLocalAddress()) {
                    log.warn("Link-local address access blocked: {}", ip);
                    throw new BizException(BizCodeEnum.LINK_LOCAL_ADDRESS_ACCESS_DENIED);
                }
            }
        } catch (UnknownHostException ex) {
            log.error("Unknown host: {}", host, ex);
            throw new BizException(BizCodeEnum.UNKNOWN_HOST);
        }
    }

    /**
     * 创建安全连接.
     *
     * @param url URL
     * @return InputStream
     * @throws IOException IO异常
     */
    private static InputStream createSafeConnection(String url) throws IOException {
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) parsedUrl.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setInstanceFollowRedirects(false);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP response code: " + responseCode);
        }

        return connection.getInputStream();
    }

}
