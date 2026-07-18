/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.iwindplus.base.mybatis.domain.property.MybatisProperty;
import com.iwindplus.base.mybatis.interceptor.MybatisInputInterceptor;
import com.iwindplus.base.mybatis.interceptor.MybatisOutputInterceptor;
import com.iwindplus.base.mybatis.manager.MybatisFieldCryptoManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis 字段安全管理配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({MybatisProperty.class, MybatisPlusProperties.class})
@ConditionalOnProperty(prefix = "mybatis-plus.field.crypto", name = "enabled", havingValue = "true")
public class MybatisFieldCryptoConfiguration {

    @Resource
    private MybatisPlusProperties mybatisPlusProperties;

    @Resource
    private MybatisProperty mybatisProperty;

    /**
     * mybatis 字段安全管理器.
     *
     * @return MybatisFieldCryptoManager
     */
    @Bean
    public MybatisFieldCryptoManager mybatisFieldCryptoManager() {
        final MybatisFieldCryptoManager mybatisFieldCryptoManager = new MybatisFieldCryptoManager(
            mybatisPlusProperties, mybatisProperty);
        log.info("MybatisFieldCryptoManager={}", mybatisFieldCryptoManager);
        return mybatisFieldCryptoManager;
    }

    /**
     * 入参拦截器（加密/解密/脱敏）.
     *
     * @return MybatisInputInterceptor
     */
    @Bean
    public MybatisInputInterceptor mybatisInputInterceptor() {
        final MybatisInputInterceptor mybatisInputInterceptor = new MybatisInputInterceptor();
        log.info("MybatisInputInterceptor={}", mybatisInputInterceptor);
        return mybatisInputInterceptor;
    }

    /**
     * 出参拦截器（加密/解密/脱敏）.
     *
     * @return MybatisOutputInterceptor
     */
    @Bean
    public MybatisOutputInterceptor mybatisOutputInterceptor() {
        final MybatisOutputInterceptor mybatisOutputInterceptor = new MybatisOutputInterceptor();
        log.info("MybatisOutputInterceptor={}", mybatisOutputInterceptor);
        return mybatisOutputInterceptor;
    }
}
