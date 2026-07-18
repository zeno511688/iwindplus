/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.ReplacePlaceholderInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.github.yulichang.interceptor.pagination.PageInnerInterceptorWrapper;
import com.iwindplus.base.mybatis.aspect.MybatisTransactionAspect;
import com.iwindplus.base.mybatis.domain.property.MybatisProperty;
import com.iwindplus.base.mybatis.handler.MyBatisAutoFillHandler;
import com.iwindplus.base.mybatis.handler.MybatisTenantLineHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis plus配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MybatisProperty.class)
public class MybatisPlusConfiguration {

    @Resource
    private MybatisProperty mybatisProperty;

    /**
     * 创建 MybatisTransactionAspect.
     *
     * @return MybatisTransactionAspect
     */
    @ConditionalOnProperty(prefix = "mybatis-plus.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public MybatisTransactionAspect mybatisTransactionAspect() {
        MybatisTransactionAspect mybatisTransactionAspect = new MybatisTransactionAspect();
        log.info("MybatisTransactionAspect={}", mybatisTransactionAspect);
        return mybatisTransactionAspect;
    }

    /**
     * 创建 MybatisPlusInterceptor.
     *
     * @return MybatisPlusInterceptor
     */
    @ConditionalOnProperty(prefix = "mybatis-plus.plugin", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 替换占位符
        interceptor.addInnerInterceptor(new ReplacePlaceholderInnerInterceptor());
        // 乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 分页
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setMaxLimit(Long.valueOf(Constants.DEFAULT_BATCH_SIZE));
        paginationInnerInterceptor.setOptimizeJoin(Boolean.FALSE);
        PageInnerInterceptorWrapper pageInnerInterceptorWrapper = new PageInnerInterceptorWrapper(paginationInnerInterceptor);
        interceptor.addInnerInterceptor(pageInnerInterceptorWrapper);
        // 防止全表更新与删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 多租户
        if (Boolean.TRUE.equals(this.mybatisProperty.getTenant().getEnabled())) {
            final MybatisTenantLineHandler mybatisTenantLineHandler = new MybatisTenantLineHandler(this.mybatisProperty);
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(mybatisTenantLineHandler));
        }
        log.info("MybatisPlusInterceptor={}", interceptor);
        return interceptor;
    }

    /**
     * 字段自动化填充.
     *
     * @return MyBatisAutoFillHandler
     */
    @Bean
    public MyBatisAutoFillHandler myBatisAutoFillHandler() {
        MyBatisAutoFillHandler myBatisAutoFillHandler = new MyBatisAutoFillHandler();
        log.info("MyBatisAutoFillHandler={}", myBatisAutoFillHandler);
        return myBatisAutoFillHandler;
    }

    /**
     * 创建ConfigurationCustomizer.
     *
     * @return ConfigurationCustomizer
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        // 使用mybatis-plus 内置的，创建Map下划线自动转驼峰
        return configuration -> configuration.setObjectWrapperFactory(new MybatisMapWrapperFactory());
    }

}
