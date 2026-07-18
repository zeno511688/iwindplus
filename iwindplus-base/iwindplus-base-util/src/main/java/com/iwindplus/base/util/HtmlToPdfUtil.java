/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

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
import java.net.URL;
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
     * url转pdf字节.
     *
     * @param url 路径
     */
    public static byte[] toPdfBytesByUrl(String url) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out, new WriterProperties());
            InputStream inputStream = new URL(url).openStream()) {
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
        try (InputStream inputStream = new URL(url).openStream()) {
            FilesUtil.setHttpServletResponse(fileName, response);
            HtmlConverter.convertToPdf(inputStream, response.getOutputStream());
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
        try {
            FilesUtil.setHttpServletResponse(fileName, response);
            HtmlConverter.convertToPdf(htmlContent, response.getOutputStream());
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
            throw new BizException(BizCodeEnum.HTML_TO_PDF_DOWNLOAD_ERROR);
        }
    }

}
