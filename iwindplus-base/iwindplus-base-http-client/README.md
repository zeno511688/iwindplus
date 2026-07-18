# http client模块，建议使用（【RestClient，WebClient】：支持负载均衡）
    统一方式
        1、在配置文件（yml,properties）中配置HttpClientProperty类中相关属性
        2、注入HttpClientExecutorStrategyFactory，调用方法

    一、HttpClient 工具管理器（默认注入）
        1、在配置文件（yml,properties）中配置HttpClientProperty类中相关属性
        2、注入ApacheHttpClientExecutor，调用方法
    二、OkHttp 工具管理器（默认注入）
        1、在配置文件（yml,properties）中配置HttpClientProperty类中相关属性
        2、注入OkHttpClientExecutor，调用方法
    三、RestClient 工具管理器（默认注入，依赖CloseableHttpClient）
        1、注入RestClientExecutor，调用方法
    四、WebClient 工具管理器（默认注入）
        1、在配置文件（yml,properties）中配置HttpClientProperty类中相关属性
        2、注入WebClientExecutor，调用方法