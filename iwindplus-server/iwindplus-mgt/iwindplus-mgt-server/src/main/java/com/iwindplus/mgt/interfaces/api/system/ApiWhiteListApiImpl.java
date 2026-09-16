/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.system;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.ApiWhiteListApi;
import com.iwindplus.mgt.application.query.system.security.ApiWhiteListQueryService;
import com.iwindplus.mgt.application.service.system.security.ApiWhiteListApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API白名单相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class ApiWhiteListApiImpl implements ApiWhiteListApi {

    private final ApiWhiteListApplicationService apiWhiteListApplicationService;
    private final ApiWhiteListQueryService apiWhiteListQueryService;

    @Override
    public ResultVO<List<String>> listApi() {
        List<String> data = this.apiWhiteListQueryService.listApi();
        return ResultVO.success(data);
    }
}
