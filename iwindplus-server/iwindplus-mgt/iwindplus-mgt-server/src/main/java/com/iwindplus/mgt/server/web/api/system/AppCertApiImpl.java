/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.system;

import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.AppCertApi;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.mgt.server.service.system.AppCertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用凭证相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class AppCertApiImpl implements AppCertApi {

    private final AppCertService appCertService;

    @Override
    public ResultVO<BaseSignVO> getByAccessKey(String accessKey, AppCertTypeEnum appCertType) {
        BaseSignVO data = this.appCertService.getByAccessKey(accessKey, appCertType);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<BaseSignVO> getByCertType(AppCertTypeEnum appCertType) {
        BaseSignVO data = this.appCertService.getByCertType(appCertType);
        return ResultVO.success(data);
    }
}
