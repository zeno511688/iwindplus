/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.web.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.LoginLogApi;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.server.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class LoginLogApiImpl extends BaseController implements LoginLogApi {

    private final LoginLogService loginLogService;

    @Override
    public ResultVO<Boolean> save(LoginLogDTO entity) {
        boolean data = this.loginLogService.save(entity);
        return ResultVO.success(data);
    }
}
