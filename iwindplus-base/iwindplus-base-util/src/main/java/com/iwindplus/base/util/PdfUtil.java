/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.io.resource.ResourceUtil;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * pdf工具类.
 *
 * @author zengdegui
 * @since 2025/11/21 00:42
 */
@Slf4j
public class PdfUtil {

    private PdfUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    public static final String YES = "Yes";
    public static final String OFF = "Off";

    /**
     * 表单字段替换.
     *
     * @param templateFileName 模板
     * @param variables        变量
     * @return byte[]
     */
    public static byte[] replacePdfFields(String templateFileName, Map<String, String> variables) {
        try (InputStream inputStream = ResourceUtil.getStream(templateFileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            doReplace(inputStream, outputStream, variables);

            return outputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            log.error("PDF 文本替换失败", e);
            return new byte[0];
        }
    }

    private static void doReplace(InputStream inputStream,
        OutputStream outputStream,
        Map<String, String> variables)
        throws IOException, DocumentException {

        PdfReader reader = null;
        PdfStamper stamper = null;

        try {
            reader = new PdfReader(inputStream);
            stamper = new PdfStamper(reader, outputStream);

            // 如果需要生成不可编辑PDF，可打开
            stamper.setFormFlattening(true);

            fillFields(stamper.getAcroFields(), variables);

        } finally {
            // PdfStamper 必须先关闭，否则PDF内容不会完全写入
            closeQuietly(stamper);
            closeQuietly(reader);
        }
    }

    private static void fillFields(AcroFields form,
        Map<String, String> variables)
        throws IOException, DocumentException {

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String fieldName = entry.getKey();
            int fieldType = form.getFieldType(fieldName);

            if (fieldType == AcroFields.FIELD_TYPE_NONE) {
                continue;
            }

            String value = entry.getValue();
            // 判断是否复选框
            if (fieldType == AcroFields.FIELD_TYPE_CHECKBOX) {
                form.setField(fieldName,
                    Boolean.parseBoolean(value) ? YES : OFF);
                continue;
            }

            form.setField(fieldName, value);
        }
    }

    private static void closeQuietly(PdfStamper stamper) {
        if (stamper == null) {
            return;
        }

        try {
            stamper.close();
        } catch (Exception e) {
            log.warn("关闭 PdfStamper 失败", e);
        }
    }

    private static void closeQuietly(PdfReader reader) {
        if (reader == null) {
            return;
        }

        try {
            reader.close();
        } catch (Exception e) {
            log.warn("关闭 PdfReader 失败", e);
        }
    }
}
