/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo.support;

import cn.hutool.core.collection.CollUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * mongo查询封装.
 *
 * @author zengdegui
 * @since 2026/04/14 22:53
 */
public class MongoQueryWrapper {

    private final List<Criteria> andCriteria = new ArrayList<>();
    private final List<Criteria> orCriteria = new ArrayList<>();

    private final Query query = new Query();

    /**
     * 精确匹配
     */
    public MongoQueryWrapper eq(String field, Object value) {
        if (value == null) {
            return this;
        }
        andCriteria.add(Criteria.where(field).is(value));
        return this;
    }

    /**
     * 精确匹配（带条件）
     */
    public MongoQueryWrapper eq(boolean condition, String field, Object value) {
        return condition ? eq(field, value) : this;
    }

    /**
     * 不等于
     */
    public MongoQueryWrapper ne(String field, Object value) {
        if (value == null) {
            return this;
        }
        andCriteria.add(Criteria.where(field).ne(value));
        return this;
    }

    /**
     * 不等于（带条件）
     */
    public MongoQueryWrapper ne(boolean condition, String field, Object value) {
        return condition ? ne(field, value) : this;
    }

    /**
     * 模糊查询（regex）
     */
    public MongoQueryWrapper like(String field, String value) {
        if (value == null) {
            return this;
        }
        andCriteria.add(Criteria.where(field).regex(value));
        return this;
    }

    /**
     * 模糊查询（带条件）
     */
    public MongoQueryWrapper like(boolean condition, String field, String value) {
        return condition ? like(field, value) : this;
    }

    /**
     * 左模糊
     */
    public MongoQueryWrapper likeLeft(String field, String value) {
        if (value == null) {
            return this;
        }
        andCriteria.add(Criteria.where(field).regex("^" + value));
        return this;
    }

    /**
     * 右模糊
     */
    public MongoQueryWrapper likeRight(String field, String value) {
        if (value == null) {
            return this;
        }
        andCriteria.add(Criteria.where(field).regex(value + "$"));
        return this;
    }

    /**
     * in
     */
    public MongoQueryWrapper in(String field, List<?> values) {
        if (CollUtil.isEmpty(values)) {
            return this;
        }
        andCriteria.add(Criteria.where(field).in(values));
        return this;
    }

    /**
     * in（带条件）
     */
    public MongoQueryWrapper in(boolean condition, String field, List<?> values) {
        return condition ? in(field, values) : this;
    }

    /**
     * not in
     */
    public MongoQueryWrapper notIn(String field, List<?> values) {
        if (CollUtil.isEmpty(values)) {
            return this;
        }
        andCriteria.add(Criteria.where(field).nin(values));
        return this;
    }

    /**
     * not in（带条件）
     */
    public MongoQueryWrapper notIn(boolean condition, String field, List<?> values) {
        return condition ? notIn(field, values) : this;
    }

    /**
     * 范围
     */
    public MongoQueryWrapper between(String field, Object from, Object to) {
        if (from != null && to != null) {
            andCriteria.add(Criteria.where(field).gte(from).lte(to));
        }
        return this;
    }

    /**
     * 范围（带条件）
     */
    public MongoQueryWrapper between(boolean condition, String field, Object from, Object to) {
        return condition ? between(field, from, to) : this;
    }

    /**
     * 大于
     */
    public MongoQueryWrapper gt(String field, Object value) {
        if (value != null) {
            andCriteria.add(Criteria.where(field).gt(value));
        }
        return this;
    }

    /**
     * 大于（带条件）
     */
    public MongoQueryWrapper gt(boolean condition, String field, Object value) {
        return condition ? gt(field, value) : this;
    }

    /**
     * 大于等于
     */
    public MongoQueryWrapper ge(String field, Object value) {
        if (value != null) {
            andCriteria.add(Criteria.where(field).gte(value));
        }
        return this;
    }

    /**
     * 小于
     */
    public MongoQueryWrapper lt(String field, Object value) {
        if (value != null) {
            andCriteria.add(Criteria.where(field).lt(value));
        }
        return this;
    }

    /**
     * 小于等于
     */
    public MongoQueryWrapper le(String field, Object value) {
        if (value != null) {
            andCriteria.add(Criteria.where(field).lte(value));
        }
        return this;
    }

    /**
     * exists
     */
    public MongoQueryWrapper exists(String field) {
        andCriteria.add(Criteria.where(field).exists(true));
        return this;
    }

    /**
     * exists（带条件）
     */
    public MongoQueryWrapper exists(boolean condition, String field) {
        return condition ? exists(field) : this;
    }

    /**
     * not exists
     */
    public MongoQueryWrapper notExists(String field) {
        andCriteria.add(Criteria.where(field).exists(false));
        return this;
    }

    /**
     * OR 条件
     */
    public MongoQueryWrapper orEq(String field, Object value) {
        if (value != null) {
            orCriteria.add(Criteria.where(field).is(value));
        }
        return this;
    }

    /**
     * OR 条件（带条件）
     */
    public MongoQueryWrapper orEq(boolean condition, String field, Object value) {
        return condition ? orEq(field, value) : this;
    }

    /**
     * OR in
     */
    public MongoQueryWrapper orIn(String field, List<?> values) {
        if (CollUtil.isNotEmpty(values)) {
            orCriteria.add(Criteria.where(field).in(values));
        }
        return this;
    }

    /**
     * 降序
     */
    public MongoQueryWrapper orderByDesc(String field) {
        query.with(Sort.by(Sort.Direction.DESC, field));
        return this;
    }

    /**
     * 排序
     */
    public MongoQueryWrapper orderByAsc(String field) {
        query.with(Sort.by(Sort.Direction.ASC, field));
        return this;
    }

    /**
     * 多字段降序
     */
    public MongoQueryWrapper orderByDesc(String... fields) {
        for (String field : fields) {
            orderByDesc(field);
        }
        return this;
    }

    /**
     * 多字段升序
     */
    public MongoQueryWrapper orderByAsc(String... fields) {
        for (String field : fields) {
            orderByAsc(field);
        }
        return this;
    }

    /**
     * 限制条数
     */
    public MongoQueryWrapper limit(int size) {
        query.limit(size);
        return this;
    }

    /**
     * 跳过
     */
    public MongoQueryWrapper skip(long skip) {
        query.skip(skip);
        return this;
    }

    /**
     * limit + skip
     */
    public MongoQueryWrapper limit(int skip, int size) {
        query.skip(skip);
        query.limit(size);
        return this;
    }

    /**
     * 字段过滤（包含）
     */
    public MongoQueryWrapper include(String... fields) {
        if (fields != null) {
            for (String field : fields) {
                query.fields().include(field);
            }
        }
        return this;
    }

    /**
     * 字段过滤（排除）
     */
    public MongoQueryWrapper exclude(String... fields) {
        if (fields != null) {
            for (String field : fields) {
                query.fields().exclude(field);
            }
        }
        return this;
    }

    /**
     * 构建 Query
     */
    public Query build() {
        // AND 条件
        if (CollUtil.isNotEmpty(andCriteria)) {
            if (andCriteria.size() == 1) {
                query.addCriteria(andCriteria.get(0));
            } else {
                query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
            }
        }

        // OR 条件
        if (CollUtil.isNotEmpty(orCriteria)) {
            query.addCriteria(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
        }

        return query;
    }
}