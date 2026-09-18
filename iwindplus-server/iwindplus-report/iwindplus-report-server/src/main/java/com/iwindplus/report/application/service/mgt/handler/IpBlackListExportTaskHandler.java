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
import com.iwindplus.mgt.api.system.dto.IpBlackListSearchDTO;
import com.iwindplus.mgt.api.system.vo.IpBlackListPageVO;
import com.iwindplus.report.application.service.mgt.vo.IpBlackListExportVO;
import com.iwindplus.mgt.client.system.IpBlackListClient;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * IP黑名单导出任务处理器.
 *
 * @author zengdegui
 * @since 2026/09/16 22:19
 */
@Component
@RequiredArgsConstructor
public class IpBlackListExportTaskHandler implements ExportTaskHandler<IpBlackListSearchDTO, IpBlackListPageVO, IpBlackListExportVO> {

    private final IpBlackListClient ipBlackListClient;

    @Override
    public Class<IpBlackListSearchDTO> getQueryClass() {
        return IpBlackListSearchDTO.class;
    }

    @Override
    public Class<IpBlackListExportVO> getRowClass() {
        return IpBlackListExportVO.class;
    }

    @Override
    public String getFileName() {
        final Locale locale = Locale.forLanguageTag(HeaderContextHolder.getContext().get(HttpHeaders.ACCEPT_LANGUAGE));
        String code = "export.fileName.ipBlackList";
        String defaultMessage = "ip黑名单";
        return MessageSourceHolder.MESSAGE_SOURCE.getMessage(
            code + FileTypeEnum.XLSX.getSuffix(),
            null,
            defaultMessage + FileTypeEnum.XLSX.getSuffix(),
            locale);
    }

    @Override
    public DbPageVO<IpBlackListPageVO> pageByCondition(IpBlackListSearchDTO entity) {
        final ResultVO<DbPageVO<IpBlackListPageVO>> response = this.ipBlackListClient.page(entity);
        response.errorThrow();
        return response.getBizData();
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
