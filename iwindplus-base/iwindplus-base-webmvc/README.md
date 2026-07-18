# webmvc模块
    一、全局异常处理（支持国际化）
        1、在配置文件（yml,properties）中配置GlobalErrorProperty类中相关属性
        2、注意：ResultVO国际化占位符消息配置格式如下：
            （1）bizMessage属性支持变量（示例："{0},{1}"】
            （2）bizData需为数组（示例："new Object[]{}"，存储占位符对应的数据】
    二、跨域相关配置
        1、在配置文件（yml,properties）中配置CrossProperty类中相关属性
    三、统一响应体增强处理器（封装接口返回值，支持国际化）默认启用
        1、在配置文件（yml,properties）中配置ResponseBodyProperty类中相关属性
        2、支持响应体加密功能