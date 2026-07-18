# binlog模块
    1、在配置文件（yml,properties）中配置BinlogProperty类中相关属性
    2、监听BinLogEvent事件
    示例：
    @Async
    @EventListener(BinLogEvent.class)
    public void onApplicationEvent(BinLogEvent event) {
        log.info("binlog日志发布事件");

        final BinlogDTO dto = event.getLogData();
        if (Objects.isNull(dto) || CharSequenceUtil.isBlank(dto.getOp())) {
            return;
        }

        Mono.fromCallable(() -> objectMapper.writeValueAsString(dto))
            .doOnError(JsonProcessingException.class,
                e -> log.warn("json skip, data={}", dto, e))
            .onErrorComplete()
            .flatMap(json -> reactiveKafkaProducerTemplate.send(property.getConfigs().get(0).getTopicName(), json))
            .doOnNext(r -> log.info("kafka ok, t={}, p={}, o={}",
                r.recordMetadata().topic(),
                r.recordMetadata().partition(),
                r.recordMetadata().offset()))
            .doOnError(e -> log.error("kafka error, data={}", dto, e))
            .subscribe();
    }