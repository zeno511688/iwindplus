# kafka模块（根据配置动态创建topic）

    优势：
        支持多个kafka集群
        支持根据配置动态创建topic
        支持消息发送重试
        支持消息消费重试 （本地重试，失败后发送dlq）
        支持链路追踪
        支持监控打点

# 一、对接流程
      1、在配置文件（yml,properties）中配置KafkaMultiProperty类中相关属性
      2、程序中调用KafkaTemplateRouter发送mq消息
      3、通过使用@KafkaMultiListener进行监听消息

# 示例
    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.bindings[1].topic}"},
        group = "${kafka.multi.clusters.default.bindings[1].group}"
    )
    public void listenBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        log.info("登陆日志批量监听开始, size={}", records.size());
        if (records == null || records.isEmpty()) {
            return;
        }

        List<LoginLogDTO> batchList = new ArrayList<>(records.size());

        try {
            buildLoginLog(records, batchList);

            if (!batchList.isEmpty()) {
                final String name = StrUtil.lowerFirst(LoginLogDisruptorEventHandler.class.getSimpleName());
                disruptorManager.getTemplate(name).publish("kafka", "mysql", batchList);
            }

            if (ack != null) {
                ack.acknowledge();
            }

        } catch (Exception ex) {
            log.error("登陆日志批量消费失败, size={}", records.size(), ex);
            throw ex;
        }
    }