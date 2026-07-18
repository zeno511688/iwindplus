/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证相关接口.
 *
 * @author zengdegui
 * @since 2025/04/04 01:16
 */
public interface AuthorizationApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/authorization/";

    /**
     * 验证访问token是否存在.
     *
     * @param accessToken 访问token
     * @return ResultVO<UserBaseVO>
     */
    @Operation(summary = "验证访问token是否存在")
    @GetMapping(API_PREFIX + "checkAccessToken")
    ResultVO<UserBaseVO> checkAccessToken(@RequestParam(value = "accessToken") String accessToken);
}
