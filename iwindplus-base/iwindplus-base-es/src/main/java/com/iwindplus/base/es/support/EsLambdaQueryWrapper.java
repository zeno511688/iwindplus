/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.base.es.support;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.iwindplus.base.util.LambdaUtil;
import java.util.List;
import java.util.function.Consumer;

/**
 * es lambda 查询封装.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2026/04/15
 */
public class EsLambdaQueryWrapper<T> extends EsQueryWrapper {

    /**
     * 精确匹配
     *
     * @param field 字段
     * @param value 值
     * @return this
     */
    public EsLambdaQueryWrapper<T> eq(SFunction<T, ?> field, Object value) {
        super.eq(column(field), value);
        return this;
    }

    /**
     * 精确匹配（带条件）
     *
     * @param condition 条件
     * @param field     字段
     * @param value     值
     * @return this
     */
    public EsLambdaQueryWrapper<T> eq(boolean condition, SFunction<T, ?> field, Object value) {
        if (condition) {
            eq(field, value);
        }
        return this;
    }

    /**
     * 不等于
     */
    public EsLambdaQueryWrapper<T> ne(SFunction<T, ?> field, Object value) {
        super.ne(column(field), value);
        return this;
    }

    /**
     * 模糊匹配（match）
     */
    public EsLambdaQueryWrapper<T> like(SFunction<T, ?> field, String value) {
        super.like(column(field), value);
        return this;
    }

    /**
     * 前缀匹配
     */
    public EsLambdaQueryWrapper<T> prefix(SFunction<T, ?> field, String value) {
        super.prefix(column(field), value);
        return this;
    }

    /**
     * 通配符匹配
     */
    public EsLambdaQueryWrapper<T> wildcard(SFunction<T, ?> field, String value) {
        super.wildcard(column(field), value);
        return this;
    }

    /**
     * 正则匹配
     */
    public EsLambdaQueryWrapper<T> regexp(SFunction<T, ?> field, String value) {
        super.regexp(column(field), value);
        return this;
    }

    /**
     * in 查询
     */
    public EsLambdaQueryWrapper<T> in(SFunction<T, ?> field, List<?> values) {
        super.in(column(field), values);
        return this;
    }

    /**
     * not in 查询
     */
    public EsLambdaQueryWrapper<T> notIn(SFunction<T, ?> field, List<?> values) {
        super.notIn(column(field), values);
        return this;
    }

    /**
     * 区间查询
     */
    public EsLambdaQueryWrapper<T> between(SFunction<T, ?> field, Object from, Object to) {
        super.between(column(field), from, to);
        return this;
    }

    /**
     * 大于
     */
    public EsLambdaQueryWrapper<T> gt(SFunction<T, ?> field, Object value) {
        super.gt(column(field), value);
        return this;
    }

    /**
     * 大于等于
     */
    public EsLambdaQueryWrapper<T> ge(SFunction<T, ?> field, Object value) {
        super.ge(column(field), value);
        return this;
    }

    /**
     * 小于
     */
    public EsLambdaQueryWrapper<T> lt(SFunction<T, ?> field, Object value) {
        super.lt(column(field), value);
        return this;
    }

    /**
     * 小于等于
     */
    public EsLambdaQueryWrapper<T> le(SFunction<T, ?> field, Object value) {
        super.le(column(field), value);
        return this;
    }

    /**
     * 存在字段
     */
    public EsLambdaQueryWrapper<T> exists(SFunction<T, ?> field) {
        super.exists(column(field));
        return this;
    }

    /**
     * 不存在字段
     */
    public EsLambdaQueryWrapper<T> notExists(SFunction<T, ?> field) {
        super.notExists(column(field));
        return this;
    }

    /**
     * OR 查询
     */
    public EsLambdaQueryWrapper<T> or(Consumer<EsLambdaQueryWrapper<T>> consumer) {
        EsLambdaQueryWrapper<T> sub = new EsLambdaQueryWrapper<>();
        consumer.accept(sub);

        // 拿到子查询 bool
        BoolQuery subBool = sub.getBool();

        // 空查询保护（非常关键）
        if (isEmpty(subBool)) {
            return this;
        }

        // 正确姿势：should 一个 bool query
        this.getBoolBuilder().should(q -> q.bool(subBool));

        // 自动兜底（避免 should 不生效）
        this.getBoolBuilder().minimumShouldMatch("1");

        return this;
    }

    /**
     * OR 精确匹配
     */
    public EsLambdaQueryWrapper<T> shouldEq(SFunction<T, ?> field, Object value) {
        super.shouldEq(column(field), value);
        return this;
    }

    /**
     * filter 精确匹配（不参与评分）
     */
    public EsLambdaQueryWrapper<T> filterEq(SFunction<T, ?> field, Object value) {
        super.filterEq(column(field), value);
        return this;
    }

    /**
     * 倒序排序
     */
    public EsLambdaQueryWrapper<T> orderByDesc(SFunction<T, ?> field) {
        super.orderByDesc(column(field));
        return this;
    }

    /**
     * 倒序排序（指定 keyword）
     */
    public EsLambdaQueryWrapper<T> orderByDesc(SFunction<T, ?> field, boolean keyword) {
        super.orderByDesc(column(field), keyword);
        return this;
    }

    /**
     * 正序排序
     */
    public EsLambdaQueryWrapper<T> orderByAsc(SFunction<T, ?> field) {
        super.orderByAsc(column(field));
        return this;
    }

    /**
     * 正序排序（指定 keyword）
     */
    public EsLambdaQueryWrapper<T> orderByAsc(SFunction<T, ?> field, boolean keyword) {
        super.orderByAsc(column(field), keyword);
        return this;
    }

    /**
     * 最大值
     */
    public EsLambdaQueryWrapper<T> max(SFunction<T, ?> field) {
        super.max(column(field));
        return this;
    }

    /**
     * 多字段匹配
     */
    @SafeVarargs
    public final EsLambdaQueryWrapper<T> multiMatch(String text, SFunction<T, ?>... fields) {
        if (fields == null || fields.length == 0) {
            return this;
        }

        String[] cols = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            cols[i] = column(fields[i]);
        }

        super.multiMatch(text, cols);
        return this;
    }

    private String column(SFunction<T, ?> field) {
        return LambdaUtil.getFieldName(field);
    }
}