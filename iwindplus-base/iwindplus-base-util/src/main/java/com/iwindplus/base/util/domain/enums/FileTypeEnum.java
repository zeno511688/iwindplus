/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.enums;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文件类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum FileTypeEnum {
    /**
     * doc.
     */
    DOC("doc", "data:application/msword;base64,", ".doc", "application/msword"),

    /**
     * docx.
     */
    DOCX("docx", "data:application/vnd.openxmlformats-officedocument.wordprocessingml.document;base64,",
        ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

    /**
     * csv.
     */
    CSV("csv", "data:text/csv;base64,", ".csv", "text/csv"),

    /**
     * xls.
     */
    XLS("xls", "data:application/vnd.ms-excel;base64,", ".xls", "application/vnd.ms-excel"),

    /**
     * xlsx.
     */
    XLSX("xlsx", "data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,",
        ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

    /**
     * xlsm.
     */
    XLSM("xlsm", "data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,",
        ".xlsm", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

    /**
     * pdf.
     */
    PDF("pdf", "data:application/pdf;base64,", ".pdf", "application/pdf"),

    /**
     * ppt.
     */
    PPT("ppt", "data:application/vnd.ms-powerpoint;base64,", ".ppt", "application/vnd.ms-powerpoint"),

    /**
     * pptx.
     */
    PPTX("pptx", "data:application/vnd.openxmlformats-officedocument.presentationml.presentation;base64,",
        ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),

    /**
     * txt.
     */
    TXT("txt", "data:text/plain;base64,", ".txt", "text/plain"),

    /**
     * png.
     */
    PNG("png", "data:image/png;base64,", ".png", "image/png"),

    /**
     * jpg.
     */
    JPG("jpg", "data:image/jpeg;base64,", ".jpg", "image/jpeg"),

    /**
     * jpeg.
     */
    JPEG("jpeg", "data:image/jpeg;base64,", ".jpeg", "image/jpeg"),

    /**
     * gif.
     */
    GIF("gif", "data:image/gif;base64,", ".gif", "image/gif"),

    /**
     * svg.
     */
    SVG("svg", "data:image/svg+xml;base64,", ".svg", "image/svg+xml"),

    /**
     * ico.
     */
    ICO("ico", "data:image/x-icon;base64,", ".ico", "image/x-icon"),

    /**
     * bmp.
     */
    BMP("bmp", "data:image/bmp;base64,", ".bmp", "image/bmp"),

    /**
     * mp3.
     */
    MP3("mp3", "data:audio/mpeg;base64,", ".mp3", "audio/mpeg"),

    /**
     * mp4.
     */
    MP4("mp4", "data:video/mp4;base64,", ".mp4", "video/mp4"),

    /**
     * mov.
     */
    MOV("mov", "data:video/quicktime", ".mov", "video/quicktime"),

    /**
     * wmv.
     */
    WMV("wmv", "data:video/x-ms-wmv", ".wmv", "video/x-ms-wmv"),

    /**
     * m2v.
     */
    M2V("m2v", "data:video/x-matroska", ".m2v", "video/x-matroska"),

    /**
     * avi.
     */
    AVI("avi", "data:video/avi", ".avi", "video/avi"),

    /**
     * mpeg.
     */
    MPEG("mpeg", "data:video/mpeg", ".mpeg", "video/mpeg"),

    /**
     * asf.
     */
    ASF("asf", "data:video/x-ms-asf", ".asf", "video/x-ms-asf"),

    /**
     * fvi.
     */
    FVI("fvi", "data:video/isivideo", ".fvi", "video/isivideo"),

    /**
     * flv.
     */
    FLV("flv", "data:video/x-flv", ".flv", "video/x-flv"),

    /**
     * flc.
     */
    FLC("flc", "data:video/x-flic", ".flc", "video/x-flic"),

    /**
     * mkv.
     */
    MKV("mkv", "data:video/x-mkv", ".mkv", "video/x-matroska"),

    /**
     * flac.
     */
    FLAC("flac", "data:audio/x-flac", ".flac", "audio/x-flac"),

    /**
     * wav.
     */
    WAV("wav", "data:audio/wav;base64,", ".wav", "audio/wav"),

    /**
     * rm.
     */
    RM("rm", "data:audio/x-pn-realaudio", ".rm", "audio/x-pn-realaudio"),

    /**
     * rmvb.
     */
    RMVB("rmvb", "data:audio/x-pn-realaudio", ".rmvb", "audio/x-pn-realaudio");

    /**
     * 文件类型.
     */
    private final String type;

    /**
     * base64对应的前缀信息.
     */
    private final String prefix;

    /**
     * 文件后缀（包含点）.
     */
    private final String suffix;

    /**
     * 内容类型.
     */
    private final String contentType;

    /**
     * 通过类型查找枚举.
     *
     * @param type 文件类型.
     * @return FileTypeEnum
     */
    public static FileTypeEnum fromType(String type) {
        return Arrays.stream(FileTypeEnum.values())
            .filter(m -> Objects.equals(type, m.getType())).findFirst().orElse(null);
    }

    /**
     * 通过前缀查找枚举.
     *
     * @param prefix 前缀.
     * @return FileTypeEnum
     */
    public static FileTypeEnum fromPrefix(String prefix) {
        return Arrays.stream(FileTypeEnum.values())
            .filter(m -> Objects.equals(prefix, m.getPrefix())).findFirst().orElse(null);
    }

    /**
     * 通过后缀查找枚举.
     *
     * @param suffix 后缀.
     * @return FileTypeEnum
     */
    public static FileTypeEnum fromSuffix(String suffix) {
        return Arrays.stream(FileTypeEnum.values())
            .filter(m -> Objects.equals(suffix, m.getSuffix())).findFirst().orElse(null);
    }

    /**
     * 通过内容类型查找枚举.
     *
     * @param contentType 内容类型.
     * @return FileTypeEnum
     */
    public static FileTypeEnum fromContentType(String contentType) {
        return Arrays.stream(FileTypeEnum.values())
            .filter(m -> Objects.equals(contentType, m.getContentType())).findFirst().orElse(null);
    }
}