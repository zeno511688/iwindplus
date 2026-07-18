/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 默认错误控制器
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "默认错误控制器")
@Controller
public class DefaultErrorController implements ErrorController {
    private static final String ACCESS_DENIED = "[access_denied]";

    @Operation(summary = "错误")
    @RequestMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        String errorMessage = getErrorMessage(request);
        if (errorMessage.startsWith(ACCESS_DENIED)) {
            model.addAttribute("errorTitle", "拒绝访问");
            model.addAttribute("errorMessage", "您已拒绝访问.");
        } else {
            model.addAttribute("errorTitle", "错误");
            model.addAttribute("errorMessage", errorMessage);
        }
        return "error";
    }

    private String getErrorMessage(HttpServletRequest request) {
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        return StringUtils.hasText(errorMessage) ? errorMessage : "";
    }
}
