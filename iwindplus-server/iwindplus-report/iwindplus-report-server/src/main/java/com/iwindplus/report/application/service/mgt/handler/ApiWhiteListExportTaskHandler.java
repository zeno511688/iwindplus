/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.report.application.service.mgt.handler;

import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.ResultVO.MessageSourceHolder;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.support.ExportTaskHandler;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.mgt.api.system.dto.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.api.system.vo.ApiWhiteListPageVO;
import com.iwindplus.mgt.client.system.ApiWhiteListClient;
import com.iwindplus.report.application.service.mgt.vo.ApiWhiteListExportVO;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * API白名单导出任务处理器.
 *
 * @author zengdegui
 * @since 2026/09/16 22:19
 */
@Component
@RequiredArgsConstructor
public class ApiWhiteListExportTaskHandler implements ExportTaskHandler<ApiWhiteListSearchDTO, ApiWhiteListPageVO, ApiWhiteListExportVO> {

    private final ApiWhiteListClient apiWhiteListClient;

    @Override
    public Class<ApiWhiteListSearchDTO> getQueryClass() {
        return ApiWhiteListSearchDTO.class;
    }

    @Override
    public Class<ApiWhiteListExportVO> getRowClass() {
        return ApiWhiteListExportVO.class;
    }

    @Override
    public String getFileName() {
        final Locale locale = Locale.forLanguageTag(HeaderContextHolder.getContext().get(HttpHeaders.ACCEPT_LANGUAGE));
        String code = "export.fileName.apiWhiteList";
        String defaultMessage = "api白名单";
        return MessageSourceHolder.MESSAGE_SOURCE.getMessage(
            code + FileTypeEnum.XLSX.getSuffix(),
            null,
            defaultMessage + FileTypeEnum.XLSX.getSuffix(),
            locale);
    }

    @Override
    public DbPageVO<ApiWhiteListPageVO> pageByCondition(ApiWhiteListSearchDTO entity) {
        final ResultVO<DbPageVO<ApiWhiteListPageVO>> response = this.apiWhiteListClient.page(entity);
        response.errorThrow();
        return response.getBizData();}

    @Override
    public void onTaskSuccess(ExportTaskVO entity) {
        ExportTaskHandler.super.onTaskSuccess(entity);
    }

    @Override
    public void onTaskFail(ExportTaskVO entity) {
        ExportTaskHandler.super.onTaskFail(entity);
    }
}
