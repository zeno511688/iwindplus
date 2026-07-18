/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.strategy;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.iwindplus.base.domain.constant.CommonConstant.MetadataConstant;
import com.iwindplus.base.loadbalancer.domain.dto.WeightRandomDTO;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
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
 * 自定义版本加权重负载均衡器.
 *
 * @author zengdegui
 * @since 2023/10/24 00:01
 */
@Slf4j
public class CustomVersionWeightLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String serviceId;
    private final AtomicInteger position;
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    public CustomVersionWeightLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId
        , NacosDiscoveryProperties nacosDiscoveryProperties) {
        this(serviceInstanceListSupplierProvider, serviceId, ThreadLocalRandom.current().nextInt(1000));
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    public CustomVersionWeightLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId,
        int seedPosition) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(seedPosition);
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
        List<ServiceInstance> instancesToChoose = instances;
        if (!(request.getContext() instanceof DefaultRequestContext requestContext)) {
            return new DefaultResponse(this.getRandomInstance(instancesToChoose));
        }
        if (!(requestContext.getClientRequest() instanceof RequestData clientRequest)) {
            return new DefaultResponse(this.getRandomInstance(instancesToChoose));
        }

        HttpHeaders headers = clientRequest.getHeaders();
        // 先根据版本，然后根据权重筛选服务
        instancesToChoose = NacosVersionWeightLoadBalancer.getServiceInstancesByVersion(instancesToChoose, headers, nacosDiscoveryProperties);
        // 权重（如果配置了权重，则只选中权重的实例）
        ServiceInstance instance = this.getServiceInstancesByWeight(instancesToChoose);
        return new DefaultResponse(instance);
    }

    private ServiceInstance getServiceInstancesByWeight(List<ServiceInstance> instancesToChoose) {
        List<ServiceInstance> serviceInstanceList = instancesToChoose.stream().filter(instance ->
            Objects.nonNull(instance.getMetadata().get(MetadataConstant.WEIGHT))).toList();
        if (CollUtil.isNotEmpty(serviceInstanceList)) {
            final List<WeightRandomDTO.ItemWithWeight<ServiceInstance>> weightList = serviceInstanceList.stream().map(instance ->
                new WeightRandomDTO.ItemWithWeight<>(instance, Double.valueOf(instance.getMetadata().get(MetadataConstant.WEIGHT)))).toList();
            if (CollUtil.isNotEmpty(weightList)) {
                WeightRandomDTO<ServiceInstance> weightRandom = new WeightRandomDTO<>(weightList);
                return weightRandom.choose();
            }
        }
        return this.getRandomInstance(instancesToChoose);
    }

    private ServiceInstance getRandomInstance(List<ServiceInstance> instances) {
        if (instances.size() == 1) {
            return instances.get(0);
        } else {
            int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;
            return instances.get(pos % instances.size());
        }
    }
}
