/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis.interceptor;

import com.iwindplus.base.mybatis.domain.property.MybatisProperty.FieldConfig.CryptoConfig;
import com.iwindplus.base.mybatis.manager.MybatisFieldCryptoManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * MyBatis 出参拦截器（解密/脱敏）
 *
 * @author zengdegui
 * @since 2025/04/22 00:46
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class MybatisOutputInterceptor implements Interceptor {

    @Resource
    private MybatisFieldCryptoManager manager;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object data = invocation.proceed();

        final CryptoConfig cfg = this.manager.getMybatisProperty().getField().getCrypto();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return data;
        }

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        if (ms == null) {
            return data;
        }

        // 只处理 SELECT
        final SqlCommandType cmd = ms.getSqlCommandType();
        if (!SqlCommandType.SELECT.equals(cmd)) {
            return data;
        }

        // 解密
        if (Boolean.TRUE.equals(cfg.getEnabledOutputDecrypt())) {
            this.manager.processHandler(data, false, false, cfg.getEnabledOutputDecrypt());
        }

        return data;
    }

}
