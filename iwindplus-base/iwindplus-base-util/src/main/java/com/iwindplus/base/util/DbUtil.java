/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库工具类.
 *
 * @author zengdegui
 * @since 2025/11/21 00:42
 */
@Slf4j
public class DbUtil {

    /**
     * mysql.
     */
    public static final String MYSQL = "mysql";

    /**
     * mariadb.
     */
    public static final String MARIADB = "mariadb";

    /**
     * oracle.
     */
    public static final String ORACLE = "oracle";

    /**
     * postgresql.
     */
    public static final String POSTGRESQL = "postgresql";

    /**
     * sqlserver.
     */
    public static final String SQLSERVER = "sqlserver";


    private DbUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取数据库名.
     *
     * @param conn 连接
     * @return String
     */
    public static String getRealDbName(Connection conn) {
        // 1. 连接元数据（最准）
        String catalog = getCatalog(conn);
        if (StrUtil.isNotBlank(catalog)) {
            return catalog;
        }

        // 2. 从 Connection 的 URL 里截
        String url = getUrl(conn);
        if (url != null) {
            String db = parseUrl(url);
            if (db != null) {
                return db;
            }
        }

        // 3. Spring 动态数据源
        String dynamic = fromSpringDynamic();
        if (dynamic != null) {
            return dynamic;
        }

        // 4. 单数据源 spring.datasource.url
        String single = fromSpringSingle();
        if (single != null) {
            return single;
        }

        // 5. 环境变量（容器、K8s、Docker）
        String env = fromEnv();
        if (env != null) {
            return env;
        }

        // 6. 抛异常 or 默认值
        log.warn("无法获取库名，返回 default");
        return "default";
    }

    private static String getCatalog(Connection conn) {
        try {
            return conn.getCatalog();
        } catch (Exception e) {
            return null;
        }
    }

    private static String parseUrl(String url) {
        // jdbc:mysql://127.0.0.1:3306/db_name?xxx
        // jdbc:oracle:thin:@//127.0.0.1:1521/orcl
        // 支持 mysql / mariadb / oracle / postgres / sqlserver
        if (url.contains(MYSQL) || url.contains(MARIADB)) {
            return ReUtil.getGroup1(".*/([^/?]+).*", url);
        }
        if (url.contains(ORACLE)) {
            return ReUtil.getGroup1("@//[^/]+/([^?]+)", url);
        }
        if (url.contains(POSTGRESQL)) {
            return ReUtil.getGroup1(".*/([^/?]+).*", url);
        }
        if (url.contains(SQLSERVER)) {
            return ReUtil.getGroup1("databaseName=([^;&]+)", url);
        }
        return null;
    }

    /**
     * 3. Spring 多数据源 dynamic-datasource
     */
    private static String fromSpringDynamic() {
        try {
            // 当前线程绑定的是哪一条数据源
            String ds = DynamicDataSourceContextHolder.peek();
            if (StrUtil.isBlank(ds)) {
                ds = "master";
            }
            DataSourceProperty prop = SpringUtil.getBean(DynamicDataSourceProperties.class)
                .getDatasource()
                .get(ds);
            return prop != null ? parseUrl(prop.getUrl()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 4. Spring 单数据源
     */
    private static String fromSpringSingle() {
        try {
            String url = SpringUtil.getProperty("spring.datasource.url");
            return parseUrl(url);
        } catch (Exception e) {
            return null;
        }
    }

    private static String fromEnv() {
        // 支持以下优先级
        return System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") :
            System.getenv("MYSQL_DATABASE") != null ? System.getenv("MYSQL_DATABASE") :
                System.getenv("SPRING_DATASOURCE_URL") != null ? parseUrl(System.getenv("SPRING_DATASOURCE_URL")) :
                    null;
    }

    private static String getUrl(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            return meta.getURL();
        } catch (Exception e) {
            return null;
        }
    }
}
