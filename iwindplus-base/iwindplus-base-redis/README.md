# redis模块
    1. 支持jackson，protostuff，kryo，jdk 序列化存储方式
# 一、显性对接
    1、在配置文件（yml,properties）中配置Redis相关属性
    2、注入并调用RedisTemplate
        2.1 支持kryo，protostuff，jackson，jdk 存储方式默认jackson
    3、注入并调用RedissonFacade相关方法
# 二、注解缓存对接
    1、在配置文件（yml,properties）中配置redis相关属性
    2、在需要使用缓存的方法上加@Cacheable @CacheEvict @CachePut注解即可,表达式规则请百度
# 三、redis流水号对接（分布式锁实现方式）
    1、程序中注入RedissonFacade.serialNum，调用该类中方法
# 四、redis分布式锁
    1、在配置文件（yml,properties）中配置RedisProperty相关属性
    2、使用RedisLock注解
# 五、redis限流
    1、在配置文件（yml,properties）中配置RedisProperty相关属性
    2、使用RedisRateLimiter注解
# 六、redis防重，幂等
    1、在配置文件（yml,properties）中配置RedisProperty相关属性
    2、使用RedisIdempotent注解