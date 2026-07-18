# Nacos支持国际化，实现动态更新

    一、Nacos国际化对接流程
        1、在配置文件（yml,properties）中配置I18nProperty类中相关属性，
            basename填国际化文件名，如：messages（不需要后缀），
            fileSuffix填国际化后缀，如：.properties
            languages为Nacos启动要加载的语言文件。
        2、在Nacos对应的命名空间下配置basename对应的国际化文件（后缀为：properties），如：messages.properties，messages_zh_CN.properties
