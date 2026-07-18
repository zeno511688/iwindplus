/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo;

import com.iwindplus.base.mongo.domain.property.MongoProperty;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * mongo配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableMongoAuditing
@EnableConfigurationProperties(MongoProperty.class)
public class MongoConfiguration {

    @Resource
    private MongoDatabaseFactory mongoDatabaseFactory;

    @Resource
    private MongoMappingContext mongoMappingContext;

    /**
     * 创建 MappingMongoConverter.
     *
     * @return MappingMongoConverter
     */
    @Bean
    public MappingMongoConverter mappingMongoConverter() {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(this.mongoDatabaseFactory);
        MappingMongoConverter mappingMongoConverter = new MappingMongoConverter(dbRefResolver, this.mongoMappingContext);
        mappingMongoConverter.setMapKeyDotReplacement("_");
        // 设置DefaultMongoTypeMapper构造参数为null，去掉"_class"域
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        log.info("MappingMongoConverter={}", mappingMongoConverter);
        return mappingMongoConverter;
    }
}
