# 告警模块
# 一、统一方式
    1、在配置文件（yml,properties）中配置AlertProperty类中相关属性
    2、注入AlertExecutorStrategyFactory，调用方法
# 二、飞书告警对接流程
    1、在配置文件（yml,properties）中配置AlertProperty类中相关属性
    2、程序中注入AlertFeishuService，调用该类中方法
