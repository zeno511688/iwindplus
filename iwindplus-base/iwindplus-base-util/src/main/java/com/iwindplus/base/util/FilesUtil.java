/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Sets;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.domain.support.InputStreamProcessor;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.ibatis.io.Resources;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件相关工具类.
 *
 * @author zengdegui
 * @since 2021/1/11
 */
@Slf4j
public class FilesUtil extends FileUtil {

    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();

    private static final MetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

    /**
     * MultipartFile转InputStream.
     *
     * @param multipartFile 文件
     * @param processor     处理器
     * @param <T>           泛型
     * @return T
     */
    public static <T> T getInputStreamByMultipartFile(MultipartFile multipartFile,
        InputStreamProcessor<T> processor) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        try (InputStream is = multipartFile.getInputStream()) {
            return processor.process(is);
        } catch (Throwable ex) {
            log.error("Process input stream error: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * MultipartFile转byte.
     *
     * @param multipartFile 文件
     * @return byte[]
     */
    public static byte[] getBytes(MultipartFile multipartFile) {
        try {
            return multipartFile.getBytes();
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        return new byte[0];
    }

    /**
     * MultipartFile转UploadByteDTO.
     *
     * @param multipartFile 文件
     * @return UploadByteDTO
     */
    public static UploadByteDTO getUploadBytes(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return null;
        }

        final byte[] bytes = FilesUtil.getBytes(multipartFile);
        return UploadByteDTO.builder()
            .data(ArrayUtil.wrap(bytes))
            .sourceFileName(multipartFile.getOriginalFilename())
            .contentType(multipartFile.getContentType())
            .build();
    }

    /**
     * List<MultipartFile>转List<UploadByteDTO>.
     *
     * @param multipartFiles 文件列表
     * @return List<UploadByteDTO>
     */
    public static List<UploadByteDTO> listUploadBytes(List<MultipartFile> multipartFiles) {
        if (CollUtil.isEmpty(multipartFiles)) {
            return null;
        }
        List<UploadByteDTO> attachment = Lists.newArrayList();
        multipartFiles.forEach(multipartFile -> attachment.add(getUploadBytes(multipartFile)));
        return attachment;
    }

    /**
     * File转byte.
     *
     * @param file 文件
     * @return byte[]
     */
    public static byte[] getBytes(File file) {
        try {
            return FileCopyUtils.copyToByteArray(file);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        return new byte[0];
    }

    /**
     * 下载文件.
     *
     * @param inputStream 输入流（必填）
     * @param fileName    文件名，包含后缀（必填）
     * @param response    响应（必填）
     */
    public static void downloadFile(InputStream inputStream, String fileName, HttpServletResponse response) {
        try {
            setHttpServletResponse(fileName, response);

            OutputStream out = response.getOutputStream();
            FileCopyUtils.copy(inputStream, out);

            out.flush();
            response.flushBuffer();
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        } finally {
            FilesUtil.closeInputStream(inputStream);
        }
    }

    /**
     * 设置响应头，用于下载.
     *
     * @param fileName 下载文件名（包含后缀）
     * @param response 响应
     */
    public static void setHttpServletResponse(String fileName, HttpServletResponse response) {
        // 原文件后缀
        String suffix = FileUtil.getSuffix(fileName);

        // fallback 文件名（ASCII）
        String downloadFileName = "download";

        if (CharSequenceUtil.isNotBlank(suffix)) {
            downloadFileName += "." + suffix;
        }

        // RFC5987 UTF-8 文件名
        String encodedFileName = URLEncodeUtil.encode(
            fileName,
            StandardCharsets.UTF_8
        ).replace("+", "%20");

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.setContentType(
            MediaType.APPLICATION_OCTET_STREAM_VALUE
        );

        response.setHeader(
            HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
            HttpHeaders.CONTENT_DISPOSITION
        );

        String contentDisposition = String.format(
            "attachment; filename=\"%s\"; filename*=UTF-8''%s",
            downloadFileName,
            encodedFileName
        );

        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            contentDisposition
        );
    }

    /**
     * 关闭InputStream流.
     *
     * @param inputStream 输入流
     */
    public static void closeInputStream(InputStream inputStream) {
        if (Objects.nonNull(inputStream)) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                log.error(ExceptionConstant.IO_EXCEPTION, ex);
            }
        }
    }

    /**
     * 关闭outputStream流.
     *
     * @param outputStream 输出流
     */
    public static void closeOutputStream(OutputStream outputStream) {
        if (Objects.nonNull(outputStream)) {
            try {
                outputStream.close();
            } catch (IOException ex) {
                log.error(ExceptionConstant.IO_EXCEPTION, ex);
            }
        }
    }

    /**
     * 根据文件名，获取ContentType.
     *
     * @param fileName 文件名
     * @return String
     */
    public static String getContentType(String fileName) {
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(fileName);
        return String.valueOf(mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM));
    }

    public static Set<Class<?>> scanClasses(String packagePatterns, Class<?> assignableType) {
        if (CharSequenceUtil.isBlank(packagePatterns)) {
            return Sets.newHashSet();
        }

        Set<Class<?>> classes = new HashSet<>(16);
        try {
            String[] packagePatternArray = StringUtils.tokenizeToStringArray(packagePatterns,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packagePattern : packagePatternArray) {
                final String resourcePath = new StringBuilder(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)
                    .append(ClassUtils.convertClassNameToResourcePath(packagePattern))
                    .append(FileConstant.MORE_CLASS).toString();
                Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(resourcePath);
                for (Resource resource : resources) {
                    ClassMetadata classMetadata = METADATA_READER_FACTORY.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    if (assignableType == null || assignableType.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return classes;
    }
}
