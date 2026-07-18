/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * 基于磁盘流file转MultipartFile.
 *
 * @author zengdegui
 * @since 2025/11/06 23:38
 */
@Slf4j
public class PathMultipartFile implements MultipartFile {

    private final Path path;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public PathMultipartFile(Path path, String name, String contentType) {
        this.path = path;
        this.name = name;
        this.originalFilename = path.getFileName().toString();
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public long getSize() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0L;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.copy(path, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
