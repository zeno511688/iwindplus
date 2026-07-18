# 对象存储模块
   # 一、阿里云oss使用流程
      1、在配置文件（yml,properties）中配置OssProperty类中相关属性
      2、程序中注入OssAliyunService，调用该类中方法
   # 二、七牛云oss使用流程
      1、在配置文件（yml,properties）中配置OssProperty类中相关属性
      2、程序中注入OssQiniuService，调用该类中方法
   # 三、本地文件oss使用流程
      1、默认是开启的
      2、在配置文件（yml,properties）中配置MultipartProperties类中相关属性
      3、程序中注入FileService，调用该类中方法
   # 四、阿里云视频点播使用流程
      1、在配置文件（yml,properties）中配置VodProperty类中相关属性
      2、程序中注入VodAliyunService，调用该类中方法