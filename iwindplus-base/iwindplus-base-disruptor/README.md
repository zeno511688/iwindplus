# disruptor模块
# 一、对接流程
      1、在配置文件（yml,properties）中配置DisruptorMultiProperty类中相关属性
      2、配置Dtp相关线程池配置
      3、接口实现DisruptorEventHandler（事件处理）
      4、注入DisruptorManager，调用相关方法

# 配置示例：
    disruptor:
        multi:
        enabled: true
        default-name: default
        configs:
            default:
                ring-buffer-size: 8192
                wait-strategy: LITE_BLOCKING
                thread-pool-name: disruptorTaskExecutor
            gatewayLogDisruptorEventHandler:
                ring-buffer-size: 16384
                wait-strategy: BUSY_SPIN
                thread-pool-name: gateway-log-disruptor-pool
            loginLogDisruptorEventHandler:
                ring-buffer-size: 4096
                wait-strategy: BLOCKING
                thread-pool-name: login-log-disruptor-pool
            