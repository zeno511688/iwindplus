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
import com.iwindplus.base.loadbalancer.domain.enums.VersionTypeEnum;
import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty;
import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty.GrayConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import reactor.core.publisher.Mono;

/**
 * Nacos版本权重负载均衡器（支持灰度发布）.
 *
 * @author zengdegui
 * @since 2023/10/27 22:50
 */
@Slf4j
public class NacosServiceInstanceLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    /**
     * Nacos集群元数据键.
     */
    private static final String NACOS_CLUSTER_KEY = "nacos.cluster";

    /**
     * Nacos权重元数据键.
     */
    private static final String NACOS_WEIGHT_KEY = "nacos.weight";

    /**
     * Nacos健康状态元数据键.
     */
    private static final String NACOS_HEALTHY_KEY = "nacos.healthy";

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final LoadBalancerProperty loadBalancerProperty;

    public NacosServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId
        , NacosDiscoveryProperties nacosDiscoveryProperties, LoadBalancerProperty loadBalancerProperty) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
        this.loadBalancerProperty = loadBalancerProperty;
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
                CharSequenceUtil.equals(instance.getMetadata().get(NACOS_CLUSTER_KEY), currentClusterName)).toList();
            if (CollUtil.isNotEmpty(sameClusterInstances)) {
                instancesToChoose = sameClusterInstances;
            }
        }

        return this.getGrayInstanceResponse(instancesToChoose, request);
    }

    /**
     * 灰度发布实例选择.
     * <p>
     * 灰度策略：
     * 1. 白名单用户：访问灰度实例（有gray-version元数据的实例）
     * 2. 百分比用户：访问灰度实例（有gray-version元数据的实例）
     * 3. 其他用户：访问正常实例（没有gray-version元数据的实例）
     * </p>
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

        // 判断是否使用灰度版本
        boolean useGrayVersion = this.shouldUseGrayVersion(headers);

        List<ServiceInstance> targetInstances;
        if (useGrayVersion) {
            // 使用灰度实例
            targetInstances = this.filterGrayInstances(instances);
            if (CollUtil.isEmpty(targetInstances)) {
                log.error("Gray instance not found, gray request rejected, instances={}",
                    instances);

                throw new IllegalStateException("No gray instances available");
            }

            log.debug("Gray load balancer selected {} gray instances",
                targetInstances.size());
        } else {
            // 使用正常实例
            targetInstances = this.filterNormalInstances(instances);
            if (CollUtil.isEmpty(targetInstances)) {
                log.error("Normal instance not found, normal request rejected, instances={}",
                    instances);

                throw new IllegalStateException("No normal instances available");
            }
        }

        // 使用默认策略（权重随机）
        ServiceInstance instance = ExtendBalancer.getServiceInstancesByWeight(targetInstances);
        return new DefaultResponse(instance);
    }

    /**
     * 获取灰度版本.
     * <p>
     * 优先级：白名单 > 百分比
     * </p>
     *
     * @param headers 请求头
     * @return true表示使用灰度版本，false表示不使用灰度
     */
    private boolean shouldUseGrayVersion(HttpHeaders headers) {
        final GrayConfig cfg = this.loadBalancerProperty.getGray();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return false;
        }
        // 1. 检查白名单
        final String userId = headers.getFirst(HeaderConstant.X_USER_ID);
        if (CharSequenceUtil.isNotBlank(userId)) {
            List<String> whitelist = cfg.getUserIdWhitelist();
            if (CollUtil.isNotEmpty(whitelist) && whitelist.contains(userId)) {
                log.debug("User {} in whitelist, use gray version", userId);
                return true;
            }
        }

        // 2. 检查百分比
        if (CharSequenceUtil.isNotBlank(userId)) {
            final Integer percentage = cfg.getPercentage();
            if (Objects.nonNull(percentage) && percentage > 0 && percentage <= 100) {
                // 使用用户ID哈希值计算百分比
                int hash = Math.abs(userId.hashCode());
                int mod = hash % 100;
                if (mod < percentage) {
                    log.debug("User {} in percentage {}%, use gray version", userId, percentage);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 筛选灰度实例（version=gray的实例）.
     *
     * @param instances 所有实例
     * @return 灰度实例列表
     */
    private List<ServiceInstance> filterGrayInstances(List<ServiceInstance> instances) {
        return instances.stream()
            .filter(instance -> {
                Map<String, String> metadata = instance.getMetadata();
                if (metadata == null) {
                    return false;
                }
                String version = metadata.get(HeaderConstant.X_VERSION);
                return VersionTypeEnum.GRAY.getValue().equals(version);
            })
            .toList();
    }

    /**
     * 筛选非灰度实例.
     *
     * @param instances 所有实例
     * @return 正常实例列表
     */
    private List<ServiceInstance> filterNormalInstances(List<ServiceInstance> instances) {
        return instances.stream()
            .filter(instance -> {
                Map<String, String> metadata = instance.getMetadata();
                if (metadata == null) {
                    return false;
                }
                String version = metadata.get(HeaderConstant.X_VERSION);
                return !VersionTypeEnum.GRAY.getValue().equals(version);
            })
            .toList();
    }

    static class ExtendBalancer extends Balancer {

        public static ServiceInstance getServiceInstancesByWeight(List<ServiceInstance> instances) {
            Map<Instance, ServiceInstance> instanceMap = new HashMap<>(16);
            List<Instance> instanceList = instances.stream().map(serviceInstance -> {
                Map<String, String> metadata = serviceInstance.getMetadata();
                Instance instance = new Instance();
                instance.setIp(serviceInstance.getHost());
                instance.setPort(serviceInstance.getPort());
                final String weightStr = metadata.get(NACOS_WEIGHT_KEY);
                instance.setWeight(CharSequenceUtil.isNotBlank(weightStr) ? Double.parseDouble(weightStr) : 1.0D);
                instance.setHealthy(Boolean.parseBoolean(metadata.get(NACOS_HEALTHY_KEY)));
                instanceMap.put(instance, serviceInstance);
                return instance;
            }).toList();
            Instance instance = getHostByRandomWeight(instanceList);
            return instanceMap.get(instance);
        }
    }
}
