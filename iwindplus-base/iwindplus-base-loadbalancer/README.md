# 微服务负载均衡模块（支持灰度发布）
    1、实现了nacos版本加权重负载均衡
    2、实现了自定义版本加权重负载均衡
    
    一、nacos负载均衡对接流程
        1、在配置文件（yml,properties）中配置loadbalancer.nacos.enable为true
    二、自定义负载均衡对接流程
        1、在配置文件（yml,properties）中配置loadbalancer.nacos.custom为true
    三、支持灰度
        nacos
            1、请求头：version
            2、Metadata nacos.weight 权重
        自定义
            1、请求头：version
            2、Metadata weight 权重