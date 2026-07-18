# shiro权限模块
ReloadPermissionManager实现了ShiroFilterFactoryBean权限过滤链热加载，当更新了数据，调用该类方法updatePermission()进行重载
# 一、有session对接
      1、在配置文件（yml,properties）中配置ShiroProperty类中相关属性
      2、程序中实现ShiroService
      3、登录接口中加入以下代码:
      Subject subject = ShiroUtil.getSubject();
      ShiroSessionTokenDTO usernamePasswordToken = new ShiroSessionTokenDTO(username, password, rememberMe);
      subject.login(usernamePasswordToken);
      4、程序中获取用户信息，调用ShiroUtil相关方法
      5、退出，subject.logout();
#  二、无session对接
      1、在配置文件（yml,properties）中配置ShiroProperty类中相关属性
      2、程序中实现ShiroService
      3、登录接口中注入ShiroRealm，并调用getAccessTokenByUsername方法生成访问token
      4、在需要登录的接口中请求头加入ShiroConstant.AUTHORIZATION参数
      5、程序中获取用户信息，调用ShiroUtil相关方法
      6、访问token失效后刷新token，注入ShiroRealm，并调用getAccessTokenByRefreshToken方法生成访问token
      7、退出，注入ShiroRealm，并调用logout方法
