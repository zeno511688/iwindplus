/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.security.handler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.domain.vo.ResultVO.MessageSourceHolder;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.support.ExportTaskHandler;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import com.iwindplus.mgt.application.query.system.security.dto.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.application.query.system.security.vo.ApiWhiteListPageVO;
import com.iwindplus.mgt.application.service.system.security.vo.ApiWhiteListExportVO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListRepository;
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

    private final ApiWhiteListRepository apiWhiteListRepository;

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
        final IPage<ApiWhiteListPageVO> page = this.apiWhiteListRepository.page(entity);
        return new DbPageVO<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public void onTaskSuccess(ExportTaskVO entity) {
        ExportTaskHandler.super.onTaskSuccess(entity);
    }

    @Override
    public void onTaskFail(ExportTaskVO entity) {
        ExportTaskHandler.super.onTaskFail(entity);
    }
}
