/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.support;

import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.DEFAULT_WEIGHT;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.HEADER_VERSION_DESC_PREFIX;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.INSTANCE_COUNT_METRIC;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.OUTCOME_EMPTY;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.OUTCOME_SUCCESS;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.PERCENTAGE_BASE;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.SELECTION_OBSERVATION;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.TAG_OUTCOME;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.TAG_ROUTE;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.TAG_SERVICE;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.TAG_VERSION;
import static com.iwindplus.base.loadbalancer.domain.constant.LoadbalancerConstant.VERSION_NONE;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.MetadataConstant;
import com.iwindplus.base.loadbalancer.domain.enums.GrayStrategyEnum;
import com.iwindplus.base.loadbalancer.domain.enums.NacosMetadataKeyEnum;
import com.iwindplus.base.loadbalancer.domain.enums.VersionTypeEnum;
import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty;
import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty.GrayConfig;
import com.iwindplus.base.monitor.executor.MonitorExecutor;
import io.micrometer.core.instrument.Tags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

/**
 * Nacos版本权重负载均衡器（支持灰度发布）.
 *
 * @author zengdegui
 * @since 2023/10/27 22:50
 */
@Slf4j
public class NacosServiceInstanceLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final LoadBalancerProperty loadBalancerProperty;
    private final MonitorExecutor monitorExecutor;
    private final Map<String, AtomicInteger> versionInstanceCounts = new ConcurrentHashMap<>();

    public NacosServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId
        , NacosDiscoveryProperties nacosDiscoveryProperties, LoadBalancerProperty loadBalancerProperty, MonitorExecutor monitorExecutor) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.loadBalancerProperty = loadBalancerProperty;
        this.monitorExecutor = monitorExecutor;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> this.getServiceInstanceResponse(request, supplier, serviceInstances));
    }

    private Response<ServiceInstance> getServiceInstanceResponse(Request<?> request, ServiceInstanceListSupplier supplier,
        List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances, request);
        if (supplier instanceof SelectedInstanceCallback obj && serviceInstanceResponse.hasServer()) {
            obj.selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request<?> request) {
        this.recordVersionInstanceCounts(instances);
        if (CollUtil.isEmpty(instances)) {
            log.warn("No servers available for service: " + this.serviceId);
            if (this.isMonitorEnabled()) {
                return getServiceInstanceResponse();
            }
            return new EmptyResponse();
        }

        // 获取当前服务所在的集群名称
        List<ServiceInstance> instancesToChoose = instances;
        String currentClusterName = this.nacosDiscoveryProperties.getClusterName();
        if (CharSequenceUtil.isBlank(currentClusterName)) {
            log.debug("Cross-cluster call for service: {}, clusterName: {}", this.serviceId, currentClusterName);
        } else {
            List<ServiceInstance> sameClusterInstances = instances.stream()
                .filter(instance -> Optional.ofNullable(instance.getMetadata())
                    .map(metadata -> metadata.get(MetadataConstant.VERSION))
                    .filter(version -> currentClusterName.equals(version))
                    .isPresent())
                .toList();
            if (CollUtil.isNotEmpty(sameClusterInstances)) {
                instancesToChoose = sameClusterInstances;
            }
        }

        // 提取HTTP请求头（入口处统一处理）
        HttpHeaders headers = null;
        if (request.getContext() instanceof DefaultRequestContext requestContext
            && requestContext.getClientRequest() instanceof RequestData clientRequest) {
            headers = clientRequest.getHeaders();
        }

        // 判断是否使用灰度实例（入口处统一判断）
        boolean useGrayInstance = false;
        if (Boolean.TRUE.equals(this.loadBalancerProperty.getGray().getEnabled()) && headers != null) {
            final GrayConfig cfg = this.loadBalancerProperty.getGray();
            useGrayInstance = this.shouldUseGrayVersion(headers, cfg);
        }

        final String version = headers == null ? null : headers.getFirst(HeaderConstant.X_VERSION);
        final String selectedVersion = useGrayInstance
            ? VersionTypeEnum.GRAY.getValue()
            : CharSequenceUtil.isNotBlank(version) ? version : VersionTypeEnum.STABLE.getValue();
        final List<ServiceInstance> selectedInstances = instancesToChoose;
        final HttpHeaders requestHeaders = headers;
        final boolean grayInstance = useGrayInstance;
        if (this.isMonitorEnabled()) {
            return getServiceInstanceResponse(selectedVersion, selectedInstances, requestHeaders, grayInstance);
        }
        return grayInstance
            ? this.getGrayInstanceResponse(selectedInstances)
            : this.getVersionInstanceResponse(selectedInstances, requestHeaders);
    }

    /**
     * 版本实例选择（非灰度模式）.
     * <p>
     * 根据请求头 X-Version 选择实例，如果请求头中没有版本信息则使用STABLE版本实例.
     * </p>
     *
     * @param instances 实例列表
     * @param headers   HTTP请求头
     * @return Response<ServiceInstance>
     */
    private Response<ServiceInstance> getVersionInstanceResponse(List<ServiceInstance> instances, HttpHeaders headers) {
        // 从请求头中获取目标版本
        final String targetVersion = headers != null ? headers.getFirst(HeaderConstant.X_VERSION) : null;
        if (CharSequenceUtil.isBlank(targetVersion)) {
            return this.selectInstancesByVersion(instances, VersionTypeEnum.STABLE.getValue(), VersionTypeEnum.STABLE.getDesc());
        }
        return this.selectInstancesByVersion(instances, targetVersion, HEADER_VERSION_DESC_PREFIX + targetVersion);
    }

    /**
     * 灰度实例选择.
     * <p>
     * 选择灰度实例（version=gray的实例），如果不存在则降级使用所有实例.
     * </p>
     *
     * @param instances 实例列表
     * @return Response<ServiceInstance>
     */
    private Response<ServiceInstance> getGrayInstanceResponse(List<ServiceInstance> instances) {
        return this.selectInstancesByVersion(instances, VersionTypeEnum.GRAY.getValue(), VersionTypeEnum.GRAY.getDesc());
    }

    /**
     * 根据版本选择实例（使用字符串版本）.
     * <p>
     * 根据指定版本筛选实例，如果不存在则降级使用所有实例.
     * </p>
     *
     * @param instances     实例列表
     * @param targetVersion 目标版本
     * @param versionDesc   版本描述（用于日志）
     * @return Response<ServiceInstance>
     */
    private Response<ServiceInstance> selectInstancesByVersion(List<ServiceInstance> instances, String targetVersion, String versionDesc) {
        // 根据版本筛选实例
        List<ServiceInstance> targetInstances = this.filterInstancesByVersion(instances, targetVersion);

        if (CollUtil.isNotEmpty(targetInstances)) {
            log.debug("Select instances by {} version, count: {}", versionDesc, targetInstances.size());
            return this.selectInstanceByWeight(targetInstances);
        }

        // 目标版本实例不存在，降级使用所有实例
        log.warn("No {} instances found, fallback to all instances", versionDesc);
        return this.selectInstanceByWeight(instances);
    }

    /**
     * 根据权重随机选择实例.
     *
     * @param instances 实例列表
     * @return Response<ServiceInstance>
     */
    private Response<ServiceInstance> selectInstanceByWeight(List<ServiceInstance> instances) {
        if (CollUtil.isEmpty(instances)) {
            log.warn("No instances available for selection");
            return new EmptyResponse();
        }

        ServiceInstance instance = ExtendBalancer.getServiceInstancesByWeight(instances);
        return new DefaultResponse(instance);
    }

    /**
     * 获取灰度版本.
     * <p>
     * 根据配置的灰度策略类型判断： - 白名单策略：用户ID在白名单中则使用灰度版本 - 百分比策略：根据用户ID哈希值按百分比分配灰度版本
     * </p>
     *
     * @param headers 请求头
     * @param cfg     配置
     * @return true表示使用灰度版本，false表示不使用灰度
     */
    private boolean shouldUseGrayVersion(HttpHeaders headers, GrayConfig cfg) {
        final String userId = headers.getFirst(HeaderConstant.X_USER_ID);
        if (CharSequenceUtil.isBlank(userId)) {
            return false;
        }

        // 根据策略类型选择不同的判断方式
        if (GrayStrategyEnum.WHITELIST.equals(cfg.getStrategy())) {
            // 白名单策略
            List<String> whitelist = cfg.getUserIdWhitelist();
            if (CollUtil.isNotEmpty(whitelist) && whitelist.contains(userId)) {
                log.debug("User {} in whitelist, use gray version", userId);
                return true;
            }
        } else if (GrayStrategyEnum.PERCENTAGE.equals(cfg.getStrategy())) {
            // 百分比策略
            final Integer percentage = cfg.getPercentage();
            if (Objects.nonNull(percentage) && percentage > 0 && percentage <= PERCENTAGE_BASE) {
                // 使用用户ID哈希值计算百分比
                int mod = Math.floorMod(userId.hashCode(), PERCENTAGE_BASE);
                if (mod < percentage) {
                    log.debug("User {} in percentage {}%, use gray version", userId, percentage);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 筛选实例（根据请求版本）.
     *
     * @param instances     所有实例
     * @param targetVersion 目标版本
     * @return 灰度实例列表
     */
    private List<ServiceInstance> filterInstancesByVersion(List<ServiceInstance> instances, String targetVersion) {
        return instances.stream()
            .filter(instance -> Optional.ofNullable(instance.getMetadata())
                .map(metadata -> metadata.get(MetadataConstant.VERSION))
                .filter(version -> targetVersion.equals(version))
                .isPresent())
            .toList();
    }

    private static final class NoAvailableInstanceException extends RuntimeException {

        private NoAvailableInstanceException(String serviceId) {
            super("No instances available for service: " + serviceId);
        }
    }

    static class ExtendBalancer extends Balancer {

        public static ServiceInstance getServiceInstancesByWeight(List<ServiceInstance> instances) {
            Map<Instance, ServiceInstance> instanceMap = new HashMap<>(16);
            List<Instance> instanceList = instances.stream().map(serviceInstance -> {
                Map<String, String> metadata = serviceInstance.getMetadata();
                Instance instance = new Instance();
                instance.setIp(serviceInstance.getHost());
                instance.setPort(serviceInstance.getPort());
                final String weightStr = metadata.get(NacosMetadataKeyEnum.WEIGHT.getValue());
                instance.setWeight(CharSequenceUtil.isNotBlank(weightStr) ? Double.parseDouble(weightStr) : DEFAULT_WEIGHT);
                instance.setHealthy(Boolean.parseBoolean(metadata.get(NacosMetadataKeyEnum.HEALTHY.getValue())));
                instanceMap.put(instance, serviceInstance);
                return instance;
            }).toList();
            Instance instance = getHostByRandomWeight(instanceList);
            return instanceMap.get(instance);
        }
    }

    private boolean isMonitorEnabled() {
        return this.monitorExecutor != null
            && this.loadBalancerProperty.getMonitor() != null
            && Boolean.TRUE.equals(this.loadBalancerProperty.getMonitor().getEnabled());
    }

    private Response<ServiceInstance> getServiceInstanceResponse(String selectedVersion, List<ServiceInstance> selectedInstances,
        HttpHeaders requestHeaders, boolean grayInstance) {
        try {
            return this.monitorExecutor.observation().execute(SELECTION_OBSERVATION, observation -> {
                observation.lowCardinalityKeyValue(TAG_SERVICE, this.serviceId);
                observation.lowCardinalityKeyValue(TAG_VERSION, selectedVersion);
                observation.lowCardinalityKeyValue(TAG_ROUTE, selectedVersion);
                Response<ServiceInstance> response = grayInstance
                    ? this.getGrayInstanceResponse(selectedInstances)
                    : this.getVersionInstanceResponse(selectedInstances, requestHeaders);
                observation.lowCardinalityKeyValue(TAG_OUTCOME, response.hasServer() ? OUTCOME_SUCCESS : OUTCOME_EMPTY);
                return response;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("Load balancer observation execution failed", e);
        }
    }

    private Response<ServiceInstance> getServiceInstanceResponse() {
        try {
            return this.monitorExecutor.observation().execute(SELECTION_OBSERVATION, observation -> {
                observation.lowCardinalityKeyValue(TAG_SERVICE, this.serviceId);
                observation.lowCardinalityKeyValue(TAG_VERSION, VERSION_NONE);
                observation.lowCardinalityKeyValue(TAG_OUTCOME, OUTCOME_EMPTY);
                throw new NoAvailableInstanceException(this.serviceId);
            });
        } catch (NoAvailableInstanceException e) {
            return new EmptyResponse();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("Load balancer observation execution failed", e);
        }
    }

    private void recordVersionInstanceCounts(List<ServiceInstance> instances) {
        if (!this.isMonitorEnabled() || CollUtil.isEmpty(instances)) {
            return;
        }
        Map<String, Integer> counts = new HashMap<>(16);
        for (ServiceInstance instance : instances) {
            String version = Optional.ofNullable(instance.getMetadata())
                .map(metadata -> metadata.get(MetadataConstant.VERSION))
                .orElse(VersionTypeEnum.STABLE.getValue());
            counts.merge(version, 1, Integer::sum);
        }
        this.versionInstanceCounts.forEach((version, value) -> value.set(counts.getOrDefault(version, 0)));
        counts.forEach((version, count) -> {
            AtomicInteger value = this.versionInstanceCounts.computeIfAbsent(version, key -> {
                AtomicInteger gaugeValue = new AtomicInteger();
                this.monitorExecutor.monitor().gauge(INSTANCE_COUNT_METRIC,
                    Tags.of(TAG_SERVICE, this.serviceId, TAG_VERSION, key), gaugeValue, AtomicInteger::get);
                return gaugeValue;
            });
            value.set(count);
        });
    }
}
