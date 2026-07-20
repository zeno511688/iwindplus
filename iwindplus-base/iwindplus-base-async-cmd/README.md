# 异步命令模块（适用本地事务 + 消息最终一致性（Outbox））：

    优势：
        支持本地消息事务提交不丢失数据
        支持动态线程池
    使用场景：支持调用其他业务失败需要重试的业务（发kafka，调支持等等）
    说明：调用提交方法，会立刻开启异步线程去回调业务接口，失败后通过配置定时任务进行重试

# 一、对接流程
  1、在配置文件（yml,properties）中配置AsyncCmdProperty类中相关属性
  2、实现AsyncCmdTaskHandler方法定制异步需要执行的业务
  3、程序中注入AsyncCmdExecutor，调用该类中submit方法提交任务
  4、配置AsyncCmdJob任务
