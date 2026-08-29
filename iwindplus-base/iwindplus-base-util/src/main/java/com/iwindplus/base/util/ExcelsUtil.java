/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.iwindplus.base.domain.constant.CommonConstant.ExcelConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ExcelImportResultBaseVO;
import com.iwindplus.base.domain.vo.ExcelImportResultVO;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.base.util.support.EasyExcelErrorRowWriteHandler;
import com.iwindplus.base.util.support.EasyExcelImportVerifyHandler;
import com.iwindplus.base.util.support.EasyExcelListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * excel工具类.
 *
 * @author zengdegui
 * @since 2023/08/22 20:43
 */
@Slf4j
public class ExcelsUtil extends ExcelUtil {

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

    /**
     * 校验excel文件格式和表头是否正确（正确返回表头列个数）.
     *
     * @param file           文件
     * @param templateSuffix 模板文件后缀
     * @param pojoClass      对象反射类
     * @param rowNum         行号（默认：0，表示第1行）
     * @return int
     */
    public static int checkExcel(MultipartFile file, String templateSuffix, Class<?> pojoClass, Integer rowNum) {
        checkFormat(file, templateSuffix);
        return FilesUtil.getInputStreamByMultipartFile(file, inputStream -> {
            if (Objects.nonNull(inputStream)) {
                // 校验表头是否正确
                return checkHeader(inputStream, pojoClass, rowNum);
            }

            return 0;
        });
    }

    /**
     * 校验excel表头是否正确（正确返回表头列个数）.
     *
     * @param inputStream 输入流
     * @param pojoClass   对象反射类
     * @param rowNum      行号（默认：0，表示第1行）
     * @return int
     */
    public static int checkHeader(InputStream inputStream, Class<?> pojoClass, Integer rowNum) {
        // 注解中所有标题
        List<String> list = listHeadByAnnotation(pojoClass);
        // excel中标题行的所有标题
        List<String> excelList = listHeadByInputStream(inputStream, rowNum);
        if (CollUtil.isEmpty(list) || CollUtil.isEmpty(excelList) || !list.containsAll(excelList)) {
            throw new BizException(BizCodeEnum.EXCEL_TEMPLATE_ERROR);
        }
        return list.size();
    }

    /**
     * 校验excel文件格式（后缀）.
     *
     * @param file           文件
     * @param templateSuffix 模板文件后缀
     */
    public static void checkFormat(MultipartFile file, String templateSuffix) {
        final String originalFilename = file.getOriginalFilename();
        final String suffix = FileNameUtil.getSuffix(originalFilename);
        // 校验文件格式是否正确
        if (!CharSequenceUtil.equalsIgnoreCase(suffix, templateSuffix)) {
            throw new BizException(BizCodeEnum.EXCEL_FORMAT_ERROR);
        }
    }

    /**
     * 获取excel中表头.
     *
     * @param inputStream 输入流
     * @param rowNum      行号（默认：0，表示第1行）
     * @return List<String>
     */
    public static List<String> listHeadByInputStream(InputStream inputStream, Integer rowNum) {
        List<String> excelList = new ArrayList<>(10);
        try (Workbook workbook = WorkbookFactory.create(inputStream);) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(Optional.ofNullable(rowNum).orElse(0));
            if (Objects.nonNull(row)) {
                for (int ii = 0; ii < row.getPhysicalNumberOfCells(); ii++) {
                    parseRow(excelList, ii, row);
                }
            }
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        return excelList;
    }

    /**
     * 获取excel中注解（@ExcelProperty）表头.
     *
     * @param pojoClass 反射类
     * @return List<String>
     */
    public static List<String> listHeadByAnnotation(Class<?> pojoClass) {
        List<String> list = new ArrayList<>(10);
        Field[] fields = pojoClass.getDeclaredFields();
        for (Field field : fields) {
            ReflectionUtils.makeAccessible(field);
            Annotation excelProperty = field.getAnnotation(ExcelProperty.class);
            if (Objects.nonNull(excelProperty)) {
                ExcelProperty ec = (ExcelProperty) excelProperty;
                String[] name = ec.value();
                StringBuilder value = new StringBuilder();
                for (String v : name) {
                    value.append(v);
                }
                list.add(value.toString());
            }
        }
        return list;
    }

    /**
     * excel设置下拉框选项.
     *
     * @param workbook 工作表（必填）
     * @param options  下拉数据选项（必填）
     * @param column   下拉列表所在列（必填，'A'表示第1列）
     * @param firstRow 下拉数据起始行（默认：0，表示第1行）
     * @param lastRow  下拉数据结束行（默认：999，表示第1000行）
     */
    public static void dropDownOption(Workbook workbook, List<String> options, char column, Integer firstRow, Integer lastRow) {
        if (CollUtil.isNotEmpty(options)) {
            Collections.sort(options);
        }
        firstRow = Optional.ofNullable(firstRow).orElse(0);
        lastRow = Optional.ofNullable(lastRow).orElse(NumberConstant.NUMBER_NINE_HUNDRED_AND_NINETY_NINE);
        final Sheet hiddenSheet = getHiddenSheet(workbook);
        String sheetName = hiddenSheet.getSheetName();
        for (int ii = 0; ii < options.size(); ii++) {
            Row row = hiddenSheet.createRow(ii);
            row.createCell(0).setCellValue(options.get(ii));
        }
        String formula = new StringBuilder(sheetName).append(ExcelConstant.FORMULA_COLUMN_A_PREFIX).append(options.size()).toString();
        createName(workbook, sheetName, formula);
        Sheet targetSheet = workbook.getSheetAt(0);
        addValidationData(targetSheet, sheetName, firstRow, lastRow, column);
    }

    /**
     * excel设置级联下拉框选项.
     *
     * @param workbook     工作表（必填）
     * @param options      下拉数据选项（必填）
     * @param parentColumn 父列（必填，'A'表示第1列）
     * @param childColumn  子列（必填，'A'表示第1列）
     * @param firstRow     下拉数据起始行（默认：0，表示第1行）
     * @param lastRow      下拉数据结束行（默认：999，表示第1000行）
     */
    public static void dropDownOption(Workbook workbook, Map<String, List<String>> options, char parentColumn, char childColumn, Integer firstRow,
        Integer lastRow) {
        firstRow = Optional.ofNullable(firstRow).orElse(0);
        lastRow = Optional.ofNullable(lastRow).orElse(NumberConstant.NUMBER_NINE_HUNDRED_AND_NINETY_NINE);
        final Sheet hiddenSheet = getHiddenSheet(workbook);
        String sheetName = hiddenSheet.getSheetName();
        int rowId = 0;
        for (Map.Entry<String, List<String>> entry : options.entrySet()) {
            String parent = entry.getKey();
            Row row = hiddenSheet.createRow(rowId++);
            row.createCell(0).setCellValue(parent);
            List<String> children = entry.getValue();
            if (CollUtil.isNotEmpty(children)) {
                Collections.sort(children);
                for (int ii = 0; ii < children.size(); ii++) {
                    row.createCell(ii + 1).setCellValue(children.get(ii));
                }
                // 添加名称管理器，1表示b列,从b列开始往后，都是子级
                String range = getRange(1, rowId, children.size());
                String formula = new StringBuilder(sheetName).append(SymbolConstant.EXCLAMATION_MARK).append(range).toString();
                createName(workbook, parent, formula);
            }
        }
        Sheet targetSheet = workbook.getSheetAt(0);
        String parentFormula = new StringBuilder(sheetName).append(ExcelConstant.FORMULA_COLUMN_A_PREFIX).append(options.size()).toString();
        createName(workbook, sheetName, parentFormula);
        addValidationData(targetSheet, sheetName, firstRow, lastRow, parentColumn);

        String childFormula = new StringBuilder(ExcelConstant.FORMULA_INDIRECT_PREFIX).append(parentColumn).append(firstRow + 1)
            .append(SymbolConstant.RIGHT_BRACKET).toString();
        addValidationData(targetSheet, childFormula, firstRow, lastRow, childColumn);
    }

    /**
     * 下载文件（excel）.
     *
     * @param workbook 工作表（必填）
     * @param fileName 文件名，包含后缀（必填）
     * @param response 响应（必填）
     */
    public static void downloadFile(Workbook workbook, String fileName, HttpServletResponse response) {
        FileTypeEnum fileType = FileTypeEnum.fromType(FileUtil.getSuffix(fileName));
        if (Objects.isNull(fileType)) {
            throw new BizException(BizCodeEnum.EXCEL_FORMAT_ERROR);
        }

        FilesUtil.setHttpServletResponse(fileName, response);
        response.setContentType(fileType.getContentType());
        try (OutputStream out = response.getOutputStream()) {
            workbook.write(out);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        } finally {
            ExcelsUtil.closeWorkbook(workbook);
        }
    }

    /**
     * 将excel转换为base64.
     *
     * @param workbook 工作表（必填）
     * @return String
     */
    public static String getBase64(Workbook workbook) {
        byte[] bytes = getByte(workbook);
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return Base64Encoder.encode(bytes);
    }

    /**
     * 将excel转换为byte[].
     *
     * @param workbook 工作表（必填）
     * @return byte[]
     */
    public static byte[] getByte(Workbook workbook) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
        }
        return new byte[0];
    }

    /**
     * 构造excel错误文件名.
     *
     * @param sourceFileName 原始文件名
     * @return String
     */
    public static String getExcelErrorFile(String sourceFileName) {
        String dateStr = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        String fileName = new StringBuilder(ExcelConstant.ERROR_FILE_PREFIX).append(FileNameUtil.mainName(sourceFileName))
            .append(SymbolConstant.UNDERLINE).append(dateStr)
            .append(SymbolConstant.POINT)
            .append(FileNameUtil.getSuffix(sourceFileName)).toString();
        log.info("错误文件名={}", fileName);
        return fileName;
    }

    /**
     * 关闭Workbook.
     *
     * @param workbook workbook
     */
    public static void closeWorkbook(Workbook workbook) {
        IosUtil.closeQuietly(workbook);
    }

    private static void parseRow(List<String> excelList, int ii, Row row) {
        if (Objects.nonNull(row.getCell(ii)) && row.getCell(ii).getCellType().equals(CellType.STRING)) {
            String value = row.getCell(ii).getStringCellValue();
            if (CharSequenceUtil.isNotBlank(value)) {
                excelList.add(value.trim());
            }
        }
    }

    private static String getRange(int offset, int rowId, int colCount) {
        char start = (char) (ExcelConstant.COLUMN_START_LETTER + offset);
        if (colCount <= NumberConstant.NUMBER_TWENTY_FIVE) {
            char end = (char) (start + colCount - 1);
            return SymbolConstant.DOLLAR + start + SymbolConstant.DOLLAR + rowId + SymbolConstant.COLON + SymbolConstant.DOLLAR + end
                + SymbolConstant.DOLLAR + rowId;
        } else {
            char endPrefix = ExcelConstant.COLUMN_START_LETTER;
            char endSuffix;
            int tmp = colCount - NumberConstant.NUMBER_TWENTY_FIVE;
            if ((tmp) / ExcelConstant.ALPHABET_LENGTH == 0 || colCount == NumberConstant.NUMBER_FIFTY_ONE) {
                // 26-51之间，包括边界（仅两次字母表计算）
                if ((tmp) % ExcelConstant.ALPHABET_LENGTH == 0) {
                    // 边界值
                    endSuffix = (char) (ExcelConstant.COLUMN_START_LETTER + NumberConstant.NUMBER_TWENTY_FIVE);
                } else {
                    endSuffix = (char) (ExcelConstant.COLUMN_START_LETTER + (tmp) % NumberConstant.NUMBER_TWENTY_FIVE);
                }
            } else {
                // 51以上
                if ((tmp) % ExcelConstant.ALPHABET_LENGTH == 0) {
                    endSuffix = (char) (ExcelConstant.COLUMN_START_LETTER + NumberConstant.NUMBER_TWENTY_FIVE);
                    endPrefix = (char) (endPrefix + (tmp) / NumberConstant.NUMBER_TWENTY_FIVE);
                } else {
                    endSuffix = (char) (ExcelConstant.COLUMN_START_LETTER + (tmp) % NumberConstant.NUMBER_TWENTY_FIVE);
                    endPrefix = (char) (endPrefix + (tmp) / ExcelConstant.ALPHABET_LENGTH);
                }
            }
            return SymbolConstant.DOLLAR + start + SymbolConstant.DOLLAR + rowId + SymbolConstant.COLON + SymbolConstant.DOLLAR + endPrefix
                + endSuffix + SymbolConstant.DOLLAR + rowId;
        }
    }

    private static void createName(Workbook workbook, String nameName, String formula) {
        Name name = workbook.createName();
        name.setNameName(nameName);
        name.setRefersToFormula(formula);
    }

    private static void hiddenSheet(Workbook workbook, Sheet sheet) {
        workbook.setSheetHidden(workbook.getSheetIndex(sheet), true);
    }

    private static Sheet getHiddenSheet(Workbook workbook) {
        String sheetName = new StringBuilder(ExcelConstant.HIDDEN_SHEET_NAME).append(workbook.getNumberOfSheets()).toString();
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.autoSizeColumn(0);
        hiddenSheet(workbook, sheet);
        return sheet;
    }

    private static void addValidationData(Sheet sheet, String formula, int firstRow, int lastRow, char column) {
        Integer col = column - ExcelConstant.COLUMN_START_LETTER;
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(formula);
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, col, col);
        DataValidation validation = helper.createValidation(constraint, regions);
        if (validation instanceof HSSFDataValidation) {
            validation.setSuppressDropDownArrow(false);
        } else {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        }
        sheet.addValidationData(validation);
    }

    private static HorizontalCellStyleStrategy buildHorizontalCellStyleStrategy() {
        // 表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        headWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        headWriteCellStyle.setWriteFont(createWriteFont((short) NumberConstant.NUMBER_FOURTEEN));

        // 内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentWriteCellStyle.setWriteFont(createWriteFont((short) NumberConstant.NUMBER_TWELVE));

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 创建字体样式.
     *
     * @param fontSize 字体大小
     * @return 字体样式
     */
    private static WriteFont createWriteFont(short fontSize) {
        WriteFont writeFont = new WriteFont();
        writeFont.setFontName(ExcelConstant.DEFAULT_FONT_NAME);
        writeFont.setFontHeightInPoints(fontSize);
        writeFont.setBold(false);
        return writeFont;
    }
}
