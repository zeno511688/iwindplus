/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用业务层抽象类.
 *
 * @param <T> 配置实体类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBaseServiceImpl<T> extends AbstractBaseConfigServiceImpl<T> {

    private final MultipartProperties multipartProperties;

    /**
     * 获取服务器上传目录（应用所在的位置）.
     *
     * @return String
     */
    protected String getRootPath() {
        return Optional.ofNullable(this.multipartProperties.getLocation()).orElse(System.getProperty(FileConstant.USER_DIR));
    }

    /**
     * 获取文件相对路径，文件名前增加日期目录.
     *
     * @param fileName 文件名，包含文件后缀（必填）
     * @param renamed 是否重命名文件名
     * @return String
     */
    protected String getRelativePath(String fileName, Boolean renamed) {
        if (CharSequenceUtil.isBlank(fileName)) {
            throw new BizException(BizCodeEnum.FILE_NAME_NOT_EMPTY);
        }

        final String suffix = FileUtil.getSuffix(fileName);
        if (CharSequenceUtil.isBlank(suffix)) {
            throw new BizException(BizCodeEnum.FILE_NAME_MUST_CONTAIN_SUFFIX);
        }

        int start = 0;
        while (start < fileName.length()
            && fileName.charAt(start) == SymbolConstant.SLASH.charAt(0)) {
            start++;
        }

        final String normalizedFileName = fileName.substring(start);

        final String targetFileName = Boolean.TRUE.equals(renamed)
            ? IdUtil.getSnowflakeNextIdStr() + SymbolConstant.POINT + suffix
            : normalizedFileName;

        return DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN)
            + SymbolConstant.SLASH
            + targetFileName;
    }

    /**
     * 重命名文件名（包含后缀）.
     *
     * @param path     路径（必填）
     * @param fileName 新文件名（必填）
     * @return String
     */
    protected String getNewFileName(String path, String fileName) {
        // 去除 URL 中的 query 参数
        final String filePath = StrUtil.subBefore(path, SymbolConstant.QUESTION_MARK, false);

        // 获取原始文件后缀
        final String suffix = FileUtil.getSuffix(filePath);
        if (CharSequenceUtil.isBlank(suffix)) {
            throw new BizException(BizCodeEnum.FILE_HAS_NOT_SUFFIX);
        }

        // 未指定新文件名，直接使用原文件名
        if (CharSequenceUtil.isBlank(fileName)) {
            return FileNameUtil.getName(filePath);
        }

        // 去除前后空格，并去掉用户传入的后缀
        final String name = FileUtil.getPrefix(fileName);
        if (CharSequenceUtil.isBlank(name)) {
            throw new BizException(BizCodeEnum.FILE_NAME_NOT_EMPTY);
        }

        // 使用原文件的后缀
        return name.trim() + SymbolConstant.POINT + suffix;
    }

    /**
     * 校验文件大小.
     *
     * @param file 文件
     */
    protected void checkFile(MultipartFile file) {
        long fileSize = file.getSize();
        long maxFileSize = this.multipartProperties.getMaxFileSize().toBytes();
        if (fileSize > maxFileSize) {
            throw new BizException(BizCodeEnum.FILE_TOO_BIG, new Object[]{fileSize});
        }
    }

    /**
     * 校验文件大小.
     *
     * @param data 字节数组
     */
    protected void checkFile(byte[] data) {
        long fileSize = data.length;
        long maxFileSize = this.multipartProperties.getMaxFileSize().toBytes();
        if (fileSize > maxFileSize) {
            throw new BizException(BizCodeEnum.FILE_TOO_BIG, new Object[]{fileSize});
        }
    }
}
