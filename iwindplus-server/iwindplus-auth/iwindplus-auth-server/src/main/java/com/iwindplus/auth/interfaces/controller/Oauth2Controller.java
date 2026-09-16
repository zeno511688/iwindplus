/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.interfaces.controller;

import com.iwindplus.auth.application.service.OauthApplicationService;
import com.iwindplus.auth.common.constant.AuthConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * oauth2控制器
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "oauth2控制器")
@RestController
@RequiredArgsConstructor
public class Oauth2Controller extends BaseController {

    private final OauthApplicationService oauthApplicationService;

    @Operation(summary = "登录")
    @GetMapping(AuthConstant.LOGIN_URL)
    public ModelAndView login(ModelAndView modelAndView) {
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @Operation(summary = "退出")
    @GetMapping(value = AuthConstant.LOGOUT_URL)
    public ResultVO<Boolean> logout() {
        this.oauthApplicationService.logout(this.getRequest());
        return ResultVO.success(Boolean.TRUE);
    }
}
