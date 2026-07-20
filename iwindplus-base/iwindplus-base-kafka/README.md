# kafka模块（根据配置动态创建topic）

    优势：
        支持多个kafka集群
        支持根据配置动态创建topic
        支持消息发送重试
        支持消息消费重试 （本地重试，失败后发送dlq）

# 一、对接流程
      1、在配置文件（yml,properties）中配置KafkaMultiProperty类中相关属性
      2、程序中调用KafkaTemplateRouter发送mq消息
      3、通过使用@KafkaMultiListener进行监听消息
