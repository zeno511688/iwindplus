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
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

/**
 * MyBatis 入参拦截器（加密/脱敏）
 *
 * @author zengdegui
 * @since 2025/04/22 00:46
 */
@Slf4j
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class MybatisInputInterceptor implements Interceptor {

    @Resource
    private MybatisFieldCryptoManager manager;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final CryptoConfig cfg = this.manager.getMybatisProperty().getField().getCrypto();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return invocation.proceed();
        }

        final Object arg = invocation.getArgs()[0];
        final Object paramObj = invocation.getArgs()[1];
        if (arg == null || paramObj == null) {
            return invocation.proceed();
        }
        if (!(arg instanceof MappedStatement ms)) {
            return invocation.proceed();
        }

        // 只处理增改
        final SqlCommandType cmd = ms.getSqlCommandType();
        if (!checkSupportedCommandType(cmd)) {
            return invocation.proceed();
        }

        // 加密/脱敏
        if (Boolean.TRUE.equals(cfg.getEnabledInputEncrypt()) || Boolean.TRUE.equals(cfg.getEnabledInputSensitive())) {
            this.manager.processHandler(paramObj, cfg.getEnabledInputEncrypt(), cfg.getEnabledInputSensitive(), false);
        }

        // 防止数据篡改
        if (Boolean.TRUE.equals(cfg.getEnabledSign())) {
            this.manager.addSign(ms, paramObj, cfg.getSecretKey(), cmd);
        }

        return invocation.proceed();
    }

    private boolean checkSupportedCommandType(SqlCommandType cmd) {
        return SqlCommandType.INSERT.equals(cmd)
            || SqlCommandType.UPDATE.equals(cmd);
    }

}
