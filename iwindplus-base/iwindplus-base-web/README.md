# web模块
    一、线程池（默认注入）（支持链路追踪）（线程变量共享，通过MDC传递）
        1. 在配置文件（yml,properties）中配置ThreadPoolProperty类中相关属性
        2、在配置文件（yml,properties）中配置TaskExecutionProperties类中相关属性，注入applicationTaskExecutor，调用方法
            若需要自定义拒绝策略，则需要实现ExecutorRejectedRunnable接口
            @Resource(name = "applicationTaskExecutor")
            private ThreadPoolTaskExecutor applicationTaskExecutor;
        3、在配置文件（yml,properties）中配置TaskSchedulingProperties类中相关属性，注入taskScheduler，调用方法        
            @Resource
            private TaskScheduler taskScheduler;
    二、默认配置XssFilter攻击过滤器
        1、在配置文件（yml,properties）中配置FilterProperty类中相关属性
    三、默认配置RequestFilter请求过滤器，只执行一次
        1、在配置文件（yml,properties）中配置FilterProperty类中相关属性
        2、请求头信息HeaderContextHolder封装
        3、用户信息UserContextHolder封装
    四、数据脱敏
        1、在配置文件（yml,properties）中配置JacksonProperty类中相关属性
        2、使用注解Sensitive
    五、KeyGenerator生成器（类名，方法名，参数，方便AOP拦截生成唯一key，支持md5加密）
    六、默认配置支持@Validated注解校验，hibernate.validator.fail_fast参数校验快速失败模式（至返回一个），还是普通模式（所有）
        1、在配置文件（yml,properties）中配置ValidatorProperty类中相关属性