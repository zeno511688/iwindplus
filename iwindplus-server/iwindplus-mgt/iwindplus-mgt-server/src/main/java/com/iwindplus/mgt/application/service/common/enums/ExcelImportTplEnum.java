/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.application.service.common.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * excel 导入模板枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum ExcelImportTplEnum implements BaseEnum<String> {
    /**
     * IP黑名单模板.
     */
    IP_BLACK_LIST_TPL("static/excel/import_ip_black_list.xlsx", "IP黑名单模板"),

    /**
     * API白名单模板.
     */
    API_WHITE_LIST_TPL("static/excel/import_api_white_list.xlsx", "API白名单模板"),

    ;

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}
