# rabbit模块

# 一、对接流程
      1、在配置文件（yml,properties）中配置RocketMultiProperty类中相关属性
      2、程序中调用RocketTemplateRouter发送mq消息
      3、通过使用@RocketMultiListener进行监听消息