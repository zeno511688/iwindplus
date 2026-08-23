/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.MetadataConstant;
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
import reactor.core.publisher.Mono;

/**
 * 自定义nacos版本加权重负载均衡器.
 *
 * @author zengdegui
 * @since 2023/10/27 22:50
 */
@Slf4j
public class NacosVersionWeightLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    public NacosVersionWeightLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId
        , NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
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
        // 先根据版本，然后根据权重筛选服务
        if (!(request.getContext() instanceof DefaultRequestContext requestContext)) {
            return new DefaultResponse(instancesToChoose.get(ThreadLocalRandom.current().nextInt(instancesToChoose.size())));
        }
        if (!(requestContext.getClientRequest() instanceof RequestData clientRequest)) {
            return new DefaultResponse(instancesToChoose.get(ThreadLocalRandom.current().nextInt(instancesToChoose.size())));
        }

        HttpHeaders headers = clientRequest.getHeaders();
        instancesToChoose = NacosVersionWeightLoadBalancer.getServiceInstancesByVersion(instancesToChoose, headers, nacosDiscoveryProperties);
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
