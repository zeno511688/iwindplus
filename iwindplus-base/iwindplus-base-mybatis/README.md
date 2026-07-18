# 建议:为了规范数据库通用字段,建议字段名统一使用，继承DbBaseDO

# mybatis数据库模块
# 一、MyBatisAutoFillHandler维护了通用字段,添加编辑时自动维护值,接口入参时可不维护通用字段值（支持字段填充策略插入时为强制还是可手动设置）
    1、在配置文件（yml,properties）中配置MybatisProperty类中相关属性
# 二、定义了假删除字段,调用remove方法只做数据假删,自定义查询sql时别漏了这个字段,用自带的方法则不需要传该字段,自动维护了,通过打印sql能看到
# 三、MybatisRedisCache支持二级缓存，在xml中引入该类
# 四、防止全表更新
# 五、分页查询最大值1000
# 六、version乐观锁
# 七、支持自定义注解数据库字段加密/解密（TableFieldSafe注解）
# 八、支持自定义注解数据库字段脱敏（TableFieldSensitive注解）
