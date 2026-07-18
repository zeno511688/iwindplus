/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.web;

import com.iwindplus.auth.domain.constant.AuthConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 授权同意控制器
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "授权同意控制器")
@Controller
@RequiredArgsConstructor
public class AuthorizationConsentController {

    private static final String EMPTY_STRING = " ";
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationConsentService authorizationConsentService;

    @Operation(summary = "确认授权")
    @GetMapping(value = AuthConstant.CONSENT_URL)
    public String consent(Principal principal, Model model,
        @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
        @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
        @RequestParam(OAuth2ParameterNames.STATE) String state,
        @RequestParam(name = OAuth2ParameterNames.USER_CODE, required = false) String userCode) {
        Set<String> scopesToApprove = new HashSet<>();
        Set<String> previouslyApprovedScopes = new HashSet<>();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        OAuth2AuthorizationConsent currentAuthorizationConsent =
            this.authorizationConsentService.findById(registeredClient.getId(), principal.getName());
        Set<String> authorizedScopes;
        if (null != currentAuthorizationConsent) {
            authorizedScopes = currentAuthorizationConsent.getScopes();
        } else {
            authorizedScopes = Collections.emptySet();
        }
        for (String requestedScope : StringUtils.delimitedListToStringArray(scope, EMPTY_STRING)) {
            if (OidcScopes.OPENID.equals(requestedScope)) {
                continue;
            }
            if (authorizedScopes.contains(requestedScope)) {
                previouslyApprovedScopes.add(requestedScope);
            } else {
                scopesToApprove.add(requestedScope);
            }
        }

        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", registeredClient.getClientName());
        model.addAttribute("state", state);
        model.addAttribute("scopes", withDescription(scopesToApprove));
        model.addAttribute("previouslyApprovedScopes", withDescription(previouslyApprovedScopes));
        model.addAttribute("principalName", principal.getName());
        model.addAttribute("userCode", userCode);
        if (StringUtils.hasText(userCode)) {
            model.addAttribute("requestURI", AuthConstant.DEVICE_VERIFICATION_URL);
        } else {
            model.addAttribute("requestURI", AuthConstant.AUTHORIZE_URL);
        }
        return "consent";
    }

    private static Set<ScopeWithDescription> withDescription(Set<String> scopes) {
        Set<ScopeWithDescription> scopeWithDescriptions = new HashSet<>();
        for (String scope : scopes) {
            scopeWithDescriptions.add(new ScopeWithDescription(scope));

        }
        return scopeWithDescriptions;
    }

    @Data
    public static class ScopeWithDescription {

        private static final String DEFAULT_DESCRIPTION = "UNKNOWN SCOPE - 我们无法提供有关此权限的信息，授予此权限时请谨慎.";
        private static final Map<String, String> SCOPE_DESCRIPTIONS = new HashMap<>();

        static {
            SCOPE_DESCRIPTIONS.put(
                OidcScopes.PROFILE,
                "此应用程序将能够读取您的个人资料信息."
            );
            SCOPE_DESCRIPTIONS.put(
                "message.read",
                "此应用程序将能够读取您的消息."
            );
            SCOPE_DESCRIPTIONS.put(
                "message.write",
                "此应用程序将能够添加新消息。它还将能够编辑和删除现有消息."
            );
            SCOPE_DESCRIPTIONS.put(
                "user.read",
                "此应用程序将能够读取您的用户信息."
            );
            SCOPE_DESCRIPTIONS.put(
                "other.scope",
                "这是作用域描述的另一个作用域示例."
            );
        }

        public final String scope;
        public final String description;

        ScopeWithDescription(String scope) {
            this.scope = scope;
            this.description = SCOPE_DESCRIPTIONS.getOrDefault(scope, DEFAULT_DESCRIPTION);
        }
    }
}
