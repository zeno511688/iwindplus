/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.excel.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.RowTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Cell;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.dto.ExcelImportResultDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;
import com.iwindplus.base.excel.handler.EasyExcelImportVerifyHandler;
import com.iwindplus.base.util.ExcelsUtil;
import com.iwindplus.base.util.ObjectEmptyCheckUtil;
import com.iwindplus.base.util.ValidUtil;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * easyexcel表格导入监听器.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2024/06/30 15:49
 */
@Slf4j
@Getter
public class EasyExcelListener<T extends ExcelImportResultDTO> extends AnalysisEventListener<T> {

    /**
     * 所有数据.
     */
    private List<T> list = Lists.newArrayList();

    /**
     * 正确数据.
     */
    private List<T> rightList = Lists.newArrayList();

    /**
     * 失败数据.
     */
    private List<T> failList = Lists.newArrayList();

    /**
     * 验证器（可选）.
     */
    private Validator validator;

    /**
     * 校验分组（可选）.
     */
    private Class<?>[] groups;

    /**
     * 反射类（可选，校验表头时用）.
     */
    private Class<?> pojoClass;

    /**
     * 自定义校验接口（可选）.
     */
    private EasyExcelImportVerifyHandler<T> verifyHandler;

    /**
     * 构造方法.
     *
     * @param validator     验证器（可选）
     * @param groups        校验分组（可选）
     * @param pojoClass     反射类（可选，校验表头时用）
     * @param verifyHandler 自定义校验接口（可选）
     */
    public EasyExcelListener(Validator validator, Class<?>[] groups, Class<?> pojoClass, EasyExcelImportVerifyHandler<T> verifyHandler) {
        this.validator = validator;
        this.groups = ArrayUtil.isNotEmpty(groups) ? groups : new Class<?>[]{Default.class};
        this.pojoClass = pojoClass;
        this.verifyHandler = verifyHandler;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        ReadRowHolder readRowHolder = context.readRowHolder();
        // 忽略空白行
        if (ObjectEmptyCheckUtil.isDeepEmpty(data) || RowTypeEnum.EMPTY == readRowHolder.getRowType()) {
            return;
        }
        this.list.add(data);
        data.setRowNum(readRowHolder.getRowIndex() + 1);
        String errorMsg = null;
        // validate注解校验
        if (Objects.nonNull(this.validator)) {
            errorMsg = ValidUtil.validateEntity(data, this.validator, this.groups);
        }
        // 自定义校验
        if (Objects.nonNull(this.verifyHandler)) {
            ExcelVerifyResultVO result = this.verifyHandler.verifyHandler(data);
            if (result != null && Boolean.FALSE.equals(result.getSuccess())) {
                errorMsg = CharSequenceUtil.isNotBlank(errorMsg)
                    ? new StringBuilder(errorMsg).append(CommonConstant.SymbolConstant.SEMICOLON).append(result.getMsg()).toString()
                    : result.getMsg();
            }
        }
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            data.setErrorMsg(errorMsg);
            this.failList.add(data);
            return;
        }
        this.rightList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // Do nothing.
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (Objects.nonNull(this.pojoClass)) {
            ReadRowHolder readRowHolder = context.readRowHolder();
            Map<Integer, Cell> cellDataMap = readRowHolder.getCellMap();
            readRowHolder.setCurrentRowAnalysisResult(cellDataMap);
            int rowIndex = readRowHolder.getRowIndex();
            int currentHeadRowNumber = context.readSheetHolder().getHeadRowNumber();
            boolean isData = rowIndex >= currentHeadRowNumber;
            if (!isData && currentHeadRowNumber != rowIndex + 1) {
                return;
            }
            List<String> valueList = Lists.newArrayList(headMap.values());
            final List<String> headList = ExcelsUtil.listHeadByAnnotation(this.pojoClass);
            if (CollUtil.isEmpty(valueList) || CollUtil.isEmpty(headList) || !valueList.containsAll(headList)) {
                throw new BizException(BizCodeEnum.EXCEL_TEMPLATE_ERROR);
            }
        }
    }
}
