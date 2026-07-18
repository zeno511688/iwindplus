/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.base.mongo.support;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.iwindplus.base.util.LambdaUtil;
import java.util.List;

/**
 * mongo lambda 查询封装.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2026/04/15
 */
public class MongoLambdaQueryWrapper<T> extends MongoQueryWrapper {

    /**
     * 获取字段名
     *
     * @param field Lambda 字段
     * @return 字段名称
     */
    private String column(SFunction<T, ?> field) {
        return LambdaUtil.getFieldName(field);
    }

    /**
     * 等于（=）
     *
     * @param field 字段
     * @param value 值（为空则忽略）
     * @return 当前对象
     */
    public MongoLambdaQueryWrapper<T> eq(SFunction<T, ?> field, Object value) {
        super.eq(column(field), value);
        return this;
    }

    /**
     * 等于（带条件）
     *
     * @param condition 是否生效
     * @param field     字段
     * @param value     值
     * @return 当前对象
     */
    public MongoLambdaQueryWrapper<T> eq(boolean condition, SFunction<T, ?> field, Object value) {
        super.eq(condition, column(field), value);
        return this;
    }

    /**
     * 不等于（!=）
     */
    public MongoLambdaQueryWrapper<T> ne(SFunction<T, ?> field, Object value) {
        super.ne(column(field), value);
        return this;
    }

    /**
     * 不等于（带条件）
     */
    public MongoLambdaQueryWrapper<T> ne(boolean condition, SFunction<T, ?> field, Object value) {
        super.ne(condition, column(field), value);
        return this;
    }

    /**
     * 模糊匹配（regex）
     *
     * @param field 字段
     * @param value 正则表达式（为空忽略）
     */
    public MongoLambdaQueryWrapper<T> like(SFunction<T, ?> field, String value) {
        super.like(column(field), value);
        return this;
    }

    /**
     * 模糊匹配（带条件）
     */
    public MongoLambdaQueryWrapper<T> like(boolean condition, SFunction<T, ?> field, String value) {
        super.like(condition, column(field), value);
        return this;
    }

    /**
     * 左模糊匹配（^value）
     */
    public MongoLambdaQueryWrapper<T> likeLeft(SFunction<T, ?> field, String value) {
        super.likeLeft(column(field), value);
        return this;
    }

    /**
     * 右模糊匹配（value$）
     */
    public MongoLambdaQueryWrapper<T> likeRight(SFunction<T, ?> field, String value) {
        super.likeRight(column(field), value);
        return this;
    }

    /**
     * IN 查询
     */
    public MongoLambdaQueryWrapper<T> in(SFunction<T, ?> field, List<?> values) {
        super.in(column(field), values);
        return this;
    }

    /**
     * IN 查询（带条件）
     */
    public MongoLambdaQueryWrapper<T> in(boolean condition, SFunction<T, ?> field, List<?> values) {
        super.in(condition, column(field), values);
        return this;
    }

    /**
     * NOT IN 查询
     */
    public MongoLambdaQueryWrapper<T> notIn(SFunction<T, ?> field, List<?> values) {
        super.notIn(column(field), values);
        return this;
    }

    /**
     * NOT IN 查询（带条件）
     */
    public MongoLambdaQueryWrapper<T> notIn(boolean condition, SFunction<T, ?> field, List<?> values) {
        super.notIn(condition, column(field), values);
        return this;
    }

    /**
     * 区间查询（between）
     */
    public MongoLambdaQueryWrapper<T> between(SFunction<T, ?> field, Object from, Object to) {
        super.between(column(field), from, to);
        return this;
    }

    /**
     * 区间查询（带条件）
     */
    public MongoLambdaQueryWrapper<T> between(boolean condition, SFunction<T, ?> field, Object from, Object to) {
        super.between(condition, column(field), from, to);
        return this;
    }

    /**
     * 大于（>）
     */
    public MongoLambdaQueryWrapper<T> gt(SFunction<T, ?> field, Object value) {
        super.gt(column(field), value);
        return this;
    }

    /**
     * 大于（带条件）
     */
    public MongoLambdaQueryWrapper<T> gt(boolean condition, SFunction<T, ?> field, Object value) {
        super.gt(condition, column(field), value);
        return this;
    }

    /**
     * 大于等于（>=）
     */
    public MongoLambdaQueryWrapper<T> ge(SFunction<T, ?> field, Object value) {
        super.ge(column(field), value);
        return this;
    }

    /**
     * 小于（<）
     */
    public MongoLambdaQueryWrapper<T> lt(SFunction<T, ?> field, Object value) {
        super.lt(column(field), value);
        return this;
    }

    /**
     * 小于等于（<=）
     */
    public MongoLambdaQueryWrapper<T> le(SFunction<T, ?> field, Object value) {
        super.le(column(field), value);
        return this;
    }

    /**
     * 字段存在
     */
    public MongoLambdaQueryWrapper<T> exists(SFunction<T, ?> field) {
        super.exists(column(field));
        return this;
    }

    /**
     * 字段存在（带条件）
     */
    public MongoLambdaQueryWrapper<T> exists(boolean condition, SFunction<T, ?> field) {
        super.exists(condition, column(field));
        return this;
    }

    /**
     * 字段不存在
     */
    public MongoLambdaQueryWrapper<T> notExists(SFunction<T, ?> field) {
        super.notExists(column(field));
        return this;
    }

    /**
     * OR 等于
     */
    public MongoLambdaQueryWrapper<T> orEq(SFunction<T, ?> field, Object value) {
        super.orEq(column(field), value);
        return this;
    }

    /**
     * OR 等于（带条件）
     */
    public MongoLambdaQueryWrapper<T> orEq(boolean condition, SFunction<T, ?> field, Object value) {
        super.orEq(condition, column(field), value);
        return this;
    }

    /**
     * OR IN
     */
    public MongoLambdaQueryWrapper<T> orIn(SFunction<T, ?> field, List<?> values) {
        super.orIn(column(field), values);
        return this;
    }

    /**
     * 按字段降序排序
     */
    public MongoLambdaQueryWrapper<T> orderByDesc(SFunction<T, ?> field) {
        super.orderByDesc(column(field));
        return this;
    }

    /**
     * 按字段升序排序
     */
    public MongoLambdaQueryWrapper<T> orderByAsc(SFunction<T, ?> field) {
        super.orderByAsc(column(field));
        return this;
    }

    /**
     * 多字段降序排序
     */
    @SafeVarargs
    public final MongoLambdaQueryWrapper<T> orderByDesc(SFunction<T, ?>... fields) {
        for (SFunction<T, ?> field : fields) {
            super.orderByDesc(column(field));
        }
        return this;
    }

    /**
     * 多字段升序排序
     */
    @SafeVarargs
    public final MongoLambdaQueryWrapper<T> orderByAsc(SFunction<T, ?>... fields) {
        for (SFunction<T, ?> field : fields) {
            super.orderByAsc(column(field));
        }
        return this;
    }

    /**
     * 包含字段
     */
    @SafeVarargs
    public final MongoLambdaQueryWrapper<T> include(SFunction<T, ?>... fields) {
        if (fields != null) {
            for (SFunction<T, ?> field : fields) {
                super.include(column(field));
            }
        }
        return this;
    }

    /**
     * 排除字段
     */
    @SafeVarargs
    public final MongoLambdaQueryWrapper<T> exclude(SFunction<T, ?>... fields) {
        if (fields != null) {
            for (SFunction<T, ?> field : fields) {
                super.exclude(column(field));
            }
        }
        return this;
    }
}