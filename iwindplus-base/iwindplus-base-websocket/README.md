# websocket模块

    一、websocket对接流程
        1、在配置文件（yml,properties）中配置WebSocketProperty类中相关属性
        2、实现IWsMsgHandler（消息处理），并注入
        3、调用Tio类的静态方法发送消息
    
    二、websocket http对接流程
        1、在配置文件（yml,properties）中配置WebSocketProperty类中相关属性
        2、实现HttpRequestHandler（消息处理），并注入
        3、调用Tio类的静态方法发送消息
        
    示例：
    WsResponse wsResponse = WsResponse.fromText(content, StandardCharsets.UTF_8.name());
    Tio.sendToUser(channelContext.tioConfig, userId.toString(), wsResponse);