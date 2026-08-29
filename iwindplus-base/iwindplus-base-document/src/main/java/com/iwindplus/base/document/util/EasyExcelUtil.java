/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.iwindplus.base.document.handler.EasyExcelErrorRowWriteHandler;
import com.iwindplus.base.document.handler.EasyExcelImportVerifyHandler;
import com.iwindplus.base.document.listener.EasyExcelListener;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ExcelImportResultBaseVO;
import com.iwindplus.base.domain.vo.ExcelImportResultVO;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.jetbrains.annotations.NotNull;

/**
 * EasyExcel工具类.
 *
 * @author zengdegui
 * @since 2024/06/30 18:03
 */
@Slf4j
public class EasyExcelUtil extends ExcelsUtil {

    /**
     * 默认的sheet名称.
     */
    public static final String DEFAULT_SHEET_NAME = "Sheet1";

    /**
     * 默认的字体名称.
     */
    public static final String DEFAULT_FONT_NAME = "宋体";

    /**
     * excel导入.
     *
     * @param inputStream   导入文件（必填）
     * @param pojoClass     反射类（可选，校验表头时用）
     * @param verifyHandler 自定义校验接口（可选）
     * @param headRowNumber 表头开始行（默认为：1）
     * @param <T>           泛型
     * @return ExcelImportResult
     */
    public static <T extends ExcelImportResultBaseVO> ExcelImportResultVO<T> importExcel(
        InputStream inputStream,
        Class<?> pojoClass,
        EasyExcelImportVerifyHandler<T> verifyHandler,
        Integer headRowNumber) {
        return importExcel(inputStream, null, null, pojoClass, verifyHandler, headRowNumber);
    }

    /**
     * excel导入.
     *
     * @param inputStream   导入文件（必填）
     * @param validator     验证器（可选）
     * @param groups        校验分组（可选）
     * @param pojoClass     反射类（可选，校验表头时用）
     * @param verifyHandler 自定义校验接口（可选）
     * @param headRowNumber 表头开始行（默认为：1）
     * @param <T>           泛型
     * @return ExcelImportResult
     */
    public static <T extends ExcelImportResultBaseVO> ExcelImportResultVO<T> importExcel(
        InputStream inputStream,
        Validator validator,
        Class<?>[] groups,
        Class<?> pojoClass,
        EasyExcelImportVerifyHandler<T> verifyHandler,
        Integer headRowNumber) {
        try {
            EasyExcelListener<T> easyExcelListener = new EasyExcelListener<>(validator, groups, pojoClass, verifyHandler);
            EasyExcelFactory.read(inputStream, pojoClass, easyExcelListener)
                .sheet()
                .headRowNumber(Optional.ofNullable(headRowNumber).orElse(1))
                .doRead();

            return ExcelImportResultVO.<T>builder()
                .list(easyExcelListener.getList())
                .rightList(easyExcelListener.getRightList())
                .failList(easyExcelListener.getFailList())
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_IMPORT_ERROR);
        }
    }

    /**
     * excel导出.
     *
     * @param response                    响应
     * @param data                        导出的数据
     * @param pojoClass                   导出的对象类型
     * @param fileName                    文件名
     * @param horizontalCellStyleStrategy 样式策略
     * @param <T>                         泛型
     */
    public static <T extends ExcelImportResultBaseVO> void exportExcel(
        HttpServletResponse response,
        List<T> data,
        Class<?> pojoClass,
        String fileName,
        HorizontalCellStyleStrategy horizontalCellStyleStrategy) {
        String sheetName = FileUtil.mainName(fileName);
        exportExcel(response, data, pojoClass, fileName, sheetName, horizontalCellStyleStrategy);
    }

    /**
     * excel导出.
     *
     * @param response                    响应
     * @param data                        导出的数据
     * @param pojoClass                   导出的对象类型
     * @param fileName                    文件名
     * @param sheetName                   sheet名
     * @param horizontalCellStyleStrategy 样式策略
     * @param <T>                         泛型
     */
    public static <T extends ExcelImportResultBaseVO> void exportExcel(
        HttpServletResponse response,
        List<T> data,
        Class<?> pojoClass,
        String fileName,
        String sheetName,
        HorizontalCellStyleStrategy horizontalCellStyleStrategy) {
        FileTypeEnum fileType = FileTypeEnum.fromType(FileUtil.getSuffix(fileName));
        if (Objects.isNull(fileType)) {
            throw new BizException(BizCodeEnum.EXCEL_FORMAT_ERROR);
        }
        try (OutputStream out = response.getOutputStream()) {
            FilesUtil.setHttpServletResponse(fileName, response);
            response.setContentType(fileType.getContentType());

            ExcelWriterBuilder write = EasyExcelFactory.write(out, pojoClass);
            List<String> errorMsgList = data.stream().map(ExcelImportResultBaseVO::getErrorMsg).filter(Objects::nonNull).toList();
            if (CollUtil.isNotEmpty(errorMsgList)) {
                // 处理校验后的错误信息
                write.registerWriteHandler(new EasyExcelErrorRowWriteHandler<>(data));
            }
            if (Objects.isNull(horizontalCellStyleStrategy)) {
                horizontalCellStyleStrategy = buildHorizontalCellStyleStrategy();
            }
            write.registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.EXCEL_EXPORT_ERROR);
        }
    }

    @NotNull
    private static HorizontalCellStyleStrategy buildHorizontalCellStyleStrategy() {
        // 表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName(DEFAULT_FONT_NAME);
        headWriteFont.setFontHeightInPoints((short) 14);
        headWriteFont.setBold(false);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontName(DEFAULT_FONT_NAME);
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteFont.setBold(false);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }
}
