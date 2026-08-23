/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.MetadataConstant;
import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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
import org.springframework.util.CollectionUtils;
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
    private final LoadBalancerProperty.GrayConfig grayConfig;

    public NacosServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId
        , NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.grayConfig = null;
    }

    public NacosServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId
        , NacosDiscoveryProperties nacosDiscoveryProperties, LoadBalancerProperty.GrayConfig grayConfig) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.grayConfig = grayConfig;
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
        if (CollUtil.isEmpty(instances)) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        
        // 获取当前服务所在的集群名称
        List<ServiceInstance> instancesToChoose = instances;
        String currentClusterName = nacosDiscoveryProperties.getClusterName();
        if (CharSequenceUtil.isBlank(currentClusterName)) {
            log.warn("A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}", this.serviceId, currentClusterName, instances);
        } else {
            List<ServiceInstance> sameClusterInstances = instances.stream().filter(instance ->
                CharSequenceUtil.equals(instance.getMetadata().get("nacos.cluster"), currentClusterName)).toList();
            if (CollUtil.isNotEmpty(sameClusterInstances)) {
                instancesToChoose = sameClusterInstances;
            }
        }
        
        // 检查是否启用灰度发布
        if (this.grayConfig != null && this.grayConfig.getEnabled() != null && this.grayConfig.getEnabled()) {
            // 使用灰度发布策略
            return this.getGrayInstanceResponse(instancesToChoose, request);
        } else {
            // 使用原有的版本+权重策略
            return this.getVersionWeightInstanceResponse(instancesToChoose, request);
        }
    }

    /**
     * 灰度发布实例选择.
     *
     * @param instances 实例列表
     * @param request   请求
     * @return Response<ServiceInstance>
     */
    private Response<ServiceInstance> getGrayInstanceResponse(List<ServiceInstance> instances, Request<?> request) {
        if (!(request.getContext() instanceof DefaultRequestContext requestContext)) {
            return new DefaultResponse(instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
        }
        if (!(requestContext.getClientRequest() instanceof RequestData clientRequest)) {
            return new DefaultResponse(instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
        }

        HttpHeaders headers = clientRequest.getHeaders();
        
        // 获取灰度版本
        String grayVersion = this.getGrayVersion(headers);
        if (CharSequenceUtil.isBlank(grayVersion)) {
            // 没有灰度版本，使用默认策略
            return this.getVersionWeightInstanceResponse(instances, request);
        }

        // 根据灰度版本过滤实例
        List<ServiceInstance> filteredInstances = this.filterInstancesByVersion(instances, grayVersion);
        if (CollUtil.isEmpty(filteredInstances)) {
            log.warn("No instances found for gray version: {}, fallback to default instances", grayVersion);
            return this.getVersionWeightInstanceResponse(instances, request);
        }

        log.debug("Gray load balancer selected {} instances for version: {}", filteredInstances.size(), grayVersion);
        
        // 使用权重随机选择
        ServiceInstance instance = ExtendBalancer.getServiceInstancesByWeight(filteredInstances);
        return new DefaultResponse(instance);
    }

    /**
     * 获取灰度版本.
     * <p>
     * 优先级：白名单 > 请求头 > 百分比
     * </p>
     *
     * @param headers 请求头
     * @return 灰度版本，null表示不使用灰度
     */
    private String getGrayVersion(HttpHeaders headers) {
        // 1. 检查白名单
        List<String> userIds = headers.get(this.grayConfig.getUserIdHeader());
        String userId = CollectionUtils.isEmpty(userIds) ? null : userIds.get(0);
        if (CharSequenceUtil.isNotBlank(userId)) {
            List<String> whitelist = this.grayConfig.getUserIdWhitelist();
            if (!CollectionUtils.isEmpty(whitelist) && whitelist.contains(userId)) {
                log.debug("User {} in whitelist, use gray version: {}", userId, this.grayConfig.getGrayVersion());
                return this.grayConfig.getGrayVersion();
            }
        }

        // 2. 检查请求头
        String grayVersion = headers.getFirst(this.grayConfig.getGrayVersionHeader());
        if (CharSequenceUtil.isNotBlank(grayVersion)) {
            log.debug("Gray version from header: {}", grayVersion);
            return grayVersion;
        }

        // 3. 检查百分比
        if (CharSequenceUtil.isNotBlank(userId)) {
            Integer percentage = this.grayConfig.getPercentage();
            if (percentage != null && percentage > 0 && percentage <= 100) {
                // 使用用户ID哈希值计算百分比
                int hash = Math.abs(userId.hashCode());
                int mod = hash % 100;
                if (mod < percentage) {
                    log.debug("User {} in percentage {}%, use gray version: {}", userId, percentage,
                        this.grayConfig.getGrayVersion());
                    return this.grayConfig.getGrayVersion();
                }
            }
        }

        // 不使用灰度
        return null;
    }

    /**
     * 根据版本过滤实例.
     *
     * @param instances 所有实例
     * @param version   目标版本
     * @return 过滤后的实例列表
     */
    private List<ServiceInstance> filterInstancesByVersion(List<ServiceInstance> instances, String version) {
        return instances.stream()
            .filter(instance -> {
                Map<String, String> metadata = instance.getMetadata();
                if (metadata == null) {
                    return false;
                }
                String instanceVersion = metadata.get(MetadataConstant.VERSION);
                return version.equals(instanceVersion);
            })
            .toList();
    }

    /**
     * 版本+权重实例选择.
     *
     * @param instances 实例列表
     * @param request   请求
     * @return Response<ServiceInstance>
     */
    private Response<ServiceInstance> getVersionWeightInstanceResponse(List<ServiceInstance> instances, Request<?> request) {
        if (!(request.getContext() instanceof DefaultRequestContext requestContext)) {
            return new DefaultResponse(instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
        }
        if (!(requestContext.getClientRequest() instanceof RequestData clientRequest)) {
            return new DefaultResponse(instances.get(ThreadLocalRandom.current().nextInt(instances.size())));
        }

        HttpHeaders headers = clientRequest.getHeaders();
        List<ServiceInstance> instancesToChoose = NacosServiceInstanceLoadBalancer.getServiceInstancesByVersion(instances, headers, nacosDiscoveryProperties);
        ServiceInstance instance = ExtendBalancer.getServiceInstancesByWeight(instancesToChoose);
        return new DefaultResponse(instance);
    }

    static List<ServiceInstance> getServiceInstancesByVersion(List<ServiceInstance> instances,
        HttpHeaders headers, NacosDiscoveryProperties nacosDiscoveryProperties) {
        String headerVersion = headers.getFirst(HeaderConstant.X_VERSION);
        String version = CharSequenceUtil.isNotBlank(headerVersion)
            ? headerVersion : nacosDiscoveryProperties.getMetadata().get(MetadataConstant.VERSION);
        log.info("负载均衡，灰度发布，{}={}", HeaderConstant.X_VERSION, version);
        if (CharSequenceUtil.isNotBlank(version)) {
            List<ServiceInstance> serviceInstances = instances.stream().filter(instance ->
                version.equals(instance.getMetadata().get(MetadataConstant.VERSION))).toList();
            if (CollUtil.isNotEmpty(serviceInstances)) {
                instances = serviceInstances;
            }
        }
        return instances;
    }

    static class ExtendBalancer extends Balancer {

        public static ServiceInstance getServiceInstancesByWeight(List<ServiceInstance> instances) {
            Map<Instance, ServiceInstance> instanceMap = new HashMap<>(16);
            List<Instance> instanceList = instances.stream().map(serviceInstance -> {
                Map<String, String> metadata = serviceInstance.getMetadata();
                Instance instance = new Instance();
                instance.setIp(serviceInstance.getHost());
                instance.setPort(serviceInstance.getPort());
                final String weightStr = metadata.get("nacos.weight");
                instance.setWeight(CharSequenceUtil.isNotBlank(weightStr) ? Double.parseDouble(weightStr) : 1.0D);
                instance.setHealthy(Boolean.parseBoolean(metadata.get("nacos.healthy")));
                instanceMap.put(instance, serviceInstance);
                return instance;
            }).toList();
            Instance instance = getHostByRandomWeight(instanceList);
            return instanceMap.get(instance);
        }
    }
}
