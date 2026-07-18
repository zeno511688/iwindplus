/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 应用凭证相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface AppCertApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/appCert/";

    /**
     * 通过访问key查找.
     *
     * @param accessKey   访问key
     * @param appCertType 应用凭证类型
     * @return ResultVO<BaseSignVO>
     */
    @Operation(summary = "通过访问key查找")
    @GetMapping(API_PREFIX + "getByAccessKey")
    ResultVO<BaseSignVO> getByAccessKey(@RequestParam(value = "accessKey") String accessKey,
        @RequestParam(value = "appCertType") AppCertTypeEnum appCertType);

    /**
     * 通过应用凭证类型查找.
     *
     * @param appCertType 应用凭证类型
     * @return ResultVO<BaseSignVO>
     */
    @Operation(summary = "通过访问key查找")
    @GetMapping(API_PREFIX + "getByCertType")
    ResultVO<BaseSignVO> getByCertType(@RequestParam(value = "appCertType") AppCertTypeEnum appCertType);
}
