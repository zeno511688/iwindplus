/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.excel.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.iwindplus.base.domain.dto.ExcelImportResultDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * 错误数据处理器（错误追加到最后一列）.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2024/06/30 17:43
 */
public class EasyExcelErrorRowWriteHandler<T extends ExcelImportResultDTO> implements RowWriteHandler {

    /**
     * 错误信息.
     */
    private List<T> errorMsgList;

    public EasyExcelErrorRowWriteHandler(List<T> errorMsgList) {
        this.errorMsgList = errorMsgList;
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex,
        Boolean isHead) {
        if (Boolean.FALSE.equals(isHead)
            && relativeRowIndex != null
            && relativeRowIndex >= 0
            && relativeRowIndex < errorMsgList.size()) {
            final T field = errorMsgList.get(relativeRowIndex);
            if (field != null && CharSequenceUtil.isNotBlank(field.getErrorMsg())) {
                Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
                CellStyle cellStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setColor(IndexedColors.RED.getIndex());
                font.setFontName("宋体");
                font.setFontHeightInPoints((short) 12);
                font.setBold(false);
                cellStyle.setFont(font);
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                // 在末尾追加错误信息字段
                Cell cell = row.createCell(Math.max(row.getLastCellNum(), 0));
                cell.setCellStyle(cellStyle);
                cell.setCellValue(field.getErrorMsg());
            }
        }
    }
}
