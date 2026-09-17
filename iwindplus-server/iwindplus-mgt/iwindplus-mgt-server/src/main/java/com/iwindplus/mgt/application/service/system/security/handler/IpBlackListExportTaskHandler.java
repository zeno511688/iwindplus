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
import com.iwindplus.mgt.application.query.system.security.dto.IpBlackListSearchDTO;
import com.iwindplus.mgt.application.query.system.security.vo.IpBlackListPageVO;
import com.iwindplus.mgt.application.service.system.security.vo.IpBlackListExportVO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListRepository;
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

    private final IpBlackListRepository ipBlackListRepository;

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
        final IPage<IpBlackListPageVO> page = this.ipBlackListRepository.page(entity);
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
