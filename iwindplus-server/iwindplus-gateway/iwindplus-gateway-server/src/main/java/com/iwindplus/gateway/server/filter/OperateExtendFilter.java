/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.gateway.server.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.gateway.server.client.MgtClient;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.ServerWebExchangeContextConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.domain.property.GatewayProperty.OperateExtendConfig;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.server.util.GatewayUtil;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 操作扩展过滤器.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Slf4j
@Component
public class OperateExtendFilter extends BaseGatewayFilter {

    private final GatewayProperty property;
    private final MgtClient mgtClient;

    public OperateExtendFilter(GatewayProperty property, MgtClient mgtClient) {
        this.property = property;
        this.mgtClient = mgtClient;
    }

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_OPERATE_EXTEND_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        OperateExtendConfig cfg = property.getOperateExtend();

        // 开关 + 白名单
        if (GatewayUtil.shouldSkip(exchange, cfg.getEnabled())) {
            return true;
        }

        // 忽略 API
        String path = exchange.getRequest().getPath().value();
        if (CollUtil.isNotEmpty(cfg.getIgnoredApi())
            && PathMatchUtil.match(cfg.getIgnoredApi(), path)) {
            return true;
        }

        UserBaseVO user = ReactorUtil.getAttribute(
            exchange,
            ServerWebExchangeContextConstant.USER_INFO
        );

        if (user != null
            && CollUtil.isNotEmpty(cfg.getIgnoredUser())
            && cfg.getIgnoredUser().contains(user.getUsername())) {
            return true;
        }

        return false;
    }

    @Override
    protected Mono<Void> filterInternal(ServerWebExchange exchange,
        GatewayFilterChain chain) {

        UserBaseVO user = ReactorUtil.getAttribute(
            exchange, ServerWebExchangeContextConstant.USER_INFO);

        if (user == null) {
            return Mono.error(new BizException(BizCodeEnum.USER_NOT_LOGIN));
        }

        return checkExtendFunction(exchange, user)
            .then(chain.filter(exchange));
    }

    private boolean matchAny(List<String> patterns, String path) {
        return CollUtil.isNotEmpty(patterns)
            && PathMatchUtil.match(patterns, path);
    }

    /**
     * 核心逻辑（已抽象）
     */
    private Mono<Void> checkExtendFunction(ServerWebExchange exchange, UserBaseVO user) {

        OperateExtendConfig cfg = property.getOperateExtend();
        String path = exchange.getRequest().getPath().value();
        HttpHeaders headers = exchange.getRequest().getHeaders();

        boolean needGa = matchAny(cfg.getIncludeGaApi(), path);
        boolean needMail = matchAny(cfg.getIncludeMailApi(), path);
        boolean needSms = matchAny(cfg.getIncludeSmsApi(), path);
        boolean needYubikey = matchAny(cfg.getIncludeYubikeyApi(), path);

        // 不需要校验
        if (!needGa && !needMail && !needSms && !needYubikey) {
            return Mono.empty();
        }

        // Header 校验
        String gaCaptcha = needGa ? requireHeader(headers, HeaderConstant.X_GA_CAPTCHA, BizCodeEnum.GA_CAPTCHA_NOT_EMPTY) : null;
        String mailCaptcha = needMail ? requireHeader(headers, HeaderConstant.X_MAIL_CAPTCHA, BizCodeEnum.MAIL_CAPTCHA_NOT_EMPTY) : null;
        String smsCaptcha = needSms ? requireHeader(headers, HeaderConstant.X_SMS_CAPTCHA, BizCodeEnum.SMS_CAPTCHA_NOT_EMPTY) : null;
        String yubikeySource = needYubikey ? requireHeader(headers, HeaderConstant.X_YUBIKEY_SOURCE, BizCodeEnum.YUBIKEY_SOURCE_NOT_EMPTY) : null;
        String yubikeySign = needYubikey ? requireHeader(headers, HeaderConstant.X_YUBIKEY_SIGN, BizCodeEnum.YUBIKEY_SIGN_NOT_EMPTY) : null;

        // 构建请求
        UserExtendFunctionValidDTO req = UserExtendFunctionValidDTO.builder()
            .userId(user.getUserId())
            .orgId(user.getOrgId())
            .gaCaptcha(gaCaptcha)
            .mailCaptcha(mailCaptcha)
            .smsCaptcha(smsCaptcha)
            .yubikeySource(yubikeySource)
            .yubikeySign(yubikeySign)
            .build();

        // 调用校验服务 + 通用校验
        return mgtClient.checkExtendFunctionByUserId(req)
            .flatMap(result -> {
                List<ExtendCheckItem> items = List.of(
                    new ExtendCheckItem(
                        needGa,
                        result::getGaBindFlag,
                        result::getGaCheckFlag,
                        BizCodeEnum.GA_UNBOUND,
                        BizCodeEnum.GA_CAPTCHA_ERROR
                    ),
                    new ExtendCheckItem(
                        needMail,
                        result::getMailBindFlag,
                        result::getMailCheckFlag,
                        BizCodeEnum.MAIL_UNBOUND,
                        BizCodeEnum.MAIL_CAPTCHA_ERROR
                    ),
                    new ExtendCheckItem(
                        needSms,
                        result::getMobileBindFlag,
                        result::getSmsCheckFlag,
                        BizCodeEnum.MOBILE_UNBOUND,
                        BizCodeEnum.SMS_CAPTCHA_ERROR
                    ),
                    new ExtendCheckItem(
                        needYubikey,
                        result::getYubikeyBindFlag,
                        result::getYubikeyCheckFlag,
                        BizCodeEnum.YUBIKEY_UNBOUND,
                        BizCodeEnum.YUBIKEY_VERIFY_ERROR
                    )
                );

                return validateAll(exchange, items);
            });
    }

    /**
     * Header 必填校验
     */
    private String requireHeader(HttpHeaders headers, String headerName, BizCodeEnum code) {
        String header = headers.getFirst(headerName);
        if (CharSequenceUtil.isBlank(header)) {
            throw new BizException(code);
        }
        return header;
    }

    private static class ExtendCheckItem {

        boolean need;
        Supplier<Boolean> bind;
        Supplier<Boolean> check;
        BizCodeEnum unbindCode;
        BizCodeEnum errorCode;

        ExtendCheckItem(
            boolean need,
            Supplier<Boolean> bind,
            Supplier<Boolean> check,
            BizCodeEnum unbindCode,
            BizCodeEnum errorCode
        ) {
            this.need = need;
            this.bind = bind;
            this.check = check;
            this.unbindCode = unbindCode;
            this.errorCode = errorCode;
        }
    }

    private Mono<Void> validateAll(
        ServerWebExchange exchange,
        List<ExtendCheckItem> items) {

        return Mono.defer(() -> {
            for (ExtendCheckItem item : items) {
                if (!item.need) {
                    continue;
                }

                Boolean bind = item.bind.get();
                Boolean check = item.check.get();

                if (Boolean.FALSE.equals(bind)) {
                    return GatewayUtil.asyncPublishErrorLog(
                        exchange,
                        property.getLog(),
                        new BizException(item.unbindCode)
                    );
                }

                if (Boolean.FALSE.equals(check)) {
                    return GatewayUtil.asyncPublishErrorLog(
                        exchange,
                        property.getLog(),
                        new BizException(item.errorCode)
                    );
                }
            }
            return Mono.empty();
        });
    }
}