# rabbit模块（根据配置动态创建绑定关系，队列，路由key）

# 一、对接流程
      1、在配置文件（yml,properties）中配置RabbitMultiProperty类中相关属性
      2、程序中调用RabbitTemplateRouter发送mq消息
      3、通过使用@RabbitMultiListener进行监听消息