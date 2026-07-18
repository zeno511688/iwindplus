/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.server.web.api;

import com.iwindplus.auth.api.AuthorizationApi;
import com.iwindplus.auth.domain.dto.OauthUserDTO;
import com.iwindplus.auth.server.util.Oauth2Util;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.BeanCopierUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class AuthorizationApiImpl implements AuthorizationApi {

    private final OAuth2AuthorizationService authorizationService;

    @Override
    public ResultVO<UserBaseVO> checkAccessToken(String accessToken) {
        final OAuth2Authorization data = this.authorizationService.findByToken(accessToken, OAuth2TokenType.ACCESS_TOKEN);
        final UserBaseVO result = this.getUserBaseVO(data);
        return ResultVO.success(result);
    }

    private UserBaseVO getUserBaseVO(OAuth2Authorization data) {
        if (Objects.isNull(data)) {
            return null;
        }

        final OauthUserDTO oauthUser = Oauth2Util.getUser(data);
        if (Objects.isNull(oauthUser)) {
            return null;
        }
        return BeanCopierUtil.copyProperties(oauthUser, UserBaseVO::new);
    }
}
