# 操作相关校验模块
    一、操作日志对接流程
        1、在配置文件（yml,properties）中配置OperateProperty类中相关属性
        2、程序中实现OperateService（日志存储持久化）
        3、在需要加入校验的接口上使用@OperateLog注解
    二、操作验证对接流程（敏感操作校验（添加，修改，删除等）时需要ga/邮箱/手机校验/yubikey，参数通过请求头传递，X-Ga-Captcha，X-Mail-Captcha，X-Sms-Captcha，X-Yubikey-Source，X-Yubikey-Sign）
        1、在配置文件（yml,properties）中配置OperateProperty类中相关属性
        2、程序中实现OperateService（校验验证码）
        3、在需要加入校验的接口上使用@OperateValid(enabledGa = true)注解
    三、API签名对接流程
        加密串=accessKey + timestamp + nonce + requestType + requestPath + params（按照字典序排序，拼接成字符串，例如："key1=value1&key2=value2"）
        备注：签名算法：MD5(HmacSHA256(加密串))
        1、在配置文件（yml,properties）中配置OperateProperty类中相关属性
        2、程序中实现ApiSignService