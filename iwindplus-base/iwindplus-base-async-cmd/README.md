# 异步命令模块（适用本地事务 + 消息最终一致性（Outbox））：

    第一次请求成功直接返回，如果失败，定时任务拉起尝试重试
    # 一、对接流程
      1、在配置文件（yml,properties）中配置AsyncCmdProperty类中相关属性
      2、实现AsyncCmdTaskHandler方法定制异步需要执行的业务
      3、程序中注入AsyncCmdExecutor，调用该类中submit方法提交任务
      4、配置AsyncCmdJob任务
