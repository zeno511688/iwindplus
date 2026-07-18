/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.Ipv4Util;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BaseEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.gateway.server.client.MgtClient;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.CacheContextConstant;
import com.iwindplus.gateway.server.domain.constant.GatewayConstant.FilterConstant;
import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.filter.base.BaseGatewayFilter;
import com.iwindplus.gateway.server.util.GatewayUtil;
import com.iwindplus.mgt.domain.dto.system.IpBlackListChangeDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * IP黑名单过滤器.
 *
 * @author zengdegui
 * @since 2024-9-24
 */
@Slf4j
@Component
public class IpBlackListFilter extends BaseGatewayFilter {

    private final GatewayProperty property;
    private final MgtClient mgtClient;
    private final AsyncLoadingCache<String, Set<String>> cache;

    public IpBlackListFilter(GatewayProperty property, MgtClient mgtClient) {
        this.property = property;
        this.mgtClient = mgtClient;
        this.cache = Caffeine.newBuilder()
            .maximumSize(property.getIpBlackList().getMaxSize())
            .expireAfterWrite(property.getIpBlackList().getCacheTimeout())
            .refreshAfterWrite(property.getIpBlackList().getCacheRefresh())
            .recordStats()
            .buildAsync((key, executor) -> loadIpBlackList().toFuture());
    }

    @Override
    public int getOrder() {
        return FilterConstant.FILTER_IP_BLACK_LIST_ORDER;
    }

    @Override
    protected boolean shouldSkip(ServerWebExchange exchange) {
        return Boolean.FALSE.equals(property.getIpBlackList().getEnabled());
    }

    @Override
    protected Mono<ServerWebExchange> before(ServerWebExchange exchange) {

        final String ip = GatewayUtil.getRealIp(exchange);

        return getIpBlackList()
            .flatMap(list -> {
                if (checkIp(list, ip)) {
                    log.warn("IP [{}] 命中黑名单，path={}",
                        ip, exchange.getRequest().getPath().value());

                    return GatewayUtil.asyncPublishErrorLog(
                        exchange,
                        property.getLog(),
                        new BizException(BizCodeEnum.IP_IS_BLACKLISTED, new Object[]{ip}));
                }

                return Mono.just(exchange);
            });
    }

    private Mono<Set<String>> getIpBlackList() {
        return Mono.fromFuture(() -> cache.get(CacheContextConstant.CACHE_KEY_ALL))
            .defaultIfEmpty(Set.of())
            .onErrorResume(e -> {
                // 异常降级：同步缓存
                Set<String> fallback = cache.synchronous().getIfPresent(CacheContextConstant.CACHE_KEY_ALL);
                if (CollUtil.isNotEmpty(fallback)) {
                    log.warn("获取 IP 黑名单失败，使用缓存降级", e);
                    return Mono.just(fallback);
                }
                log.warn("获取 IP 黑名单失败，返回空集合", e);
                return Mono.just(Set.of());
            });
    }

    private Mono<Set<String>> loadIpBlackList() {
        return mgtClient.listIp()
            .defaultIfEmpty(List.of())
            .map(list -> Set.copyOf(list))
            .doOnNext(list -> log.info("IP黑名单加载完成，条目数={}", list.size()))
            .doOnError(ex -> {
                if (ex instanceof BizException bizEx) {
                    throw bizEx;
                } else {
                    log.error("IP黑名单加载失败", ex);
                }
            });
    }

    /**
     * 刷新黑名单.
     *
     * @param message 消息
     */
    public void refreshBlackList(MessageBaseDTO<IpBlackListChangeDTO> message) {
        if (message == null || message.getOperateType() == null || message.getData() == null) {
            return;
        }
        OperateTypeEnum op = BaseEnum.fromValue(message.getOperateType(), OperateTypeEnum.class);
        IpBlackListChangeDTO data = message.getData();

        cache.synchronous().asMap().compute(CacheContextConstant.CACHE_KEY_ALL, (key, old) -> {
            Set<String> newSet = old == null ? new HashSet<>() : new HashSet<>(old);
            switch (op) {
                case ADD:
                    Optional.ofNullable(data.getNewIp()).ifPresent(newSet::addAll);
                    break;
                case DELETE:
                    Optional.ofNullable(data.getOldIp()).ifPresent(newSet::removeAll);
                    break;
                case MODIFY:
                    Optional.ofNullable(data.getOldIp()).ifPresent(newSet::removeAll);
                    Optional.ofNullable(data.getNewIp()).ifPresent(newSet::addAll);
                    break;
                default:
                    throw new BizException(BizCodeEnum.UNSUPPORTED_TYPE, message.getOperateType());
            }
            log.info("IP 黑名单刷新完成，条目数={}", newSet.size());
            return newSet;
        });
    }

    private boolean checkIp(Set<String> ipList, String ip) {
        Set<String> direct = new HashSet<>(16);
        Set<String> cidr = new HashSet<>(16);

        for (String ipStr : ipList) {
            (ipStr.contains(SymbolConstant.SLASH) ? cidr : direct).add(ipStr);
        }

        if (direct.contains(ip)) {
            return true;
        }
        if (!cidr.isEmpty()) {
            return cidr.stream().anyMatch(c -> Ipv4Util.matches(c, ip));
        }
        return false;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        log.info("开始预热加载 IP 黑名单");
        getIpBlackList().subscribe();
    }

}
