/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.excel.handler;

import com.iwindplus.base.domain.dto.ExcelImportResultDTO;
import com.iwindplus.base.domain.vo.ExcelVerifyResultVO;

/**
 * excel自定义导入校验接口.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2024/06/30 18:19
 */
public interface EasyExcelImportVerifyHandler<T extends ExcelImportResultDTO> {

    /**
     * 校验.
     *
     * @param data 数据
     * @return ExcelVerifyResultVO
     */
    ExcelVerifyResultVO verifyHandler(T data);
}
