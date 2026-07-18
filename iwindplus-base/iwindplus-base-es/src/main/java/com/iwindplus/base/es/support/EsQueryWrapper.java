/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.es.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.SourceFilter;

/**
 * es查询封装.
 *
 * @author zengdegui
 * @since 2026/04/14 22:53
 */
public class EsQueryWrapper {

    public static final String FIELD_KEYWORD = ".keyword";

    private final BoolQuery.Builder bool = new BoolQuery.Builder();

    private final List<SortOptions> sorts = new ArrayList<>(10);

    private final Map<String, Aggregation> aggregations = new LinkedHashMap<>(16);

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * searchAfter查询条件
     */
    private List<Object> searchAfter;

    /**
     * 包含字段
     */
    private String[] includes;

    /**
     * 排除字段
     */
    private String[] excludes;

    /**
     * 精确匹配
     */
    public EsQueryWrapper eq(String field, Object value) {
        if (value == null) {
            return this;
        }
        bool.must(m -> m.term(t -> t.field(field).value(FieldValue.of(value))));
        return this;
    }

    /**
     * 逻辑条件
     */
    public EsQueryWrapper eq(boolean condition, String field, Object value) {
        return condition ? eq(field, value) : this;
    }

    /**
     * 不等于
     */
    public EsQueryWrapper ne(String field, Object value) {
        if (value == null) {
            return this;
        }
        bool.mustNot(m -> m.term(t -> t.field(field).value(FieldValue.of(value))));
        return this;
    }

    /**
     * 模糊匹配（match）
     */
    public EsQueryWrapper like(String field, String value) {
        if (value == null) {
            return this;
        }
        bool.must(m -> m.match(t -> t.field(field).query(value)));
        return this;
    }

    /**
     * 多字段模糊
     */
    public EsQueryWrapper multiMatch(String text, String... fields) {
        if (text == null || fields == null) {
            return this;
        }
        bool.must(m -> m.multiMatch(mm -> mm.query(text).fields(List.of(fields))));
        return this;
    }

    /**
     * 前缀匹配
     */
    public EsQueryWrapper prefix(String field, String value) {
        bool.must(m -> m.prefix(p -> p.field(field).value(value)));
        return this;
    }

    /**
     * 通配符
     */
    public EsQueryWrapper wildcard(String field, String value) {
        bool.must(m -> m.wildcard(w -> w.field(field).value(value)));
        return this;
    }

    /**
     * 正则
     */
    public EsQueryWrapper regexp(String field, String value) {
        bool.must(m -> m.regexp(r -> r.field(field).value(value)));
        return this;
    }

    /**
     * in 查询
     */
    public EsQueryWrapper in(String field, List<?> values) {
        if (CollUtil.isEmpty(values)) {
            return this;
        }
        bool.must(m -> m.terms(t -> t
            .field(field)
            .terms(v -> v.value(values.stream().map(FieldValue::of).collect(Collectors.toList())))
        ));
        return this;
    }

    /**
     * not in
     */
    public EsQueryWrapper notIn(String field, List<?> values) {
        if (CollUtil.isEmpty(values)) {
            return this;
        }
        bool.mustNot(m -> m.terms(t -> t
            .field(field)
            .terms(v -> v.value(values.stream().map(FieldValue::of).collect(Collectors.toList())))
        ));
        return this;
    }

    /**
     * ids 查询
     */
    public EsQueryWrapper ids(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return this;
        }
        bool.must(m -> m.ids(i -> i.values(ids)));
        return this;
    }

    /**
     * exists
     */
    public EsQueryWrapper exists(String field) {
        bool.must(m -> m.exists(e -> e.field(field)));
        return this;
    }

    /**
     * not exists
     */
    public EsQueryWrapper notExists(String field) {
        bool.mustNot(m -> m.exists(e -> e.field(field)));
        return this;
    }

    /**
     * 范围查询
     */
    public EsQueryWrapper between(String field, Object from, Object to) {
        bool.must(m -> m.range(r -> r.field(field)
            .gte(JsonData.of(from))
            .lte(JsonData.of(to))
        ));
        return this;
    }

    public EsQueryWrapper gt(String field, Object value) {
        bool.must(m -> m.range(r -> r.field(field).gt(JsonData.of(value))));
        return this;
    }

    public EsQueryWrapper ge(String field, Object value) {
        if (value == null) {
            return this;
        }
        bool.must(m -> m.range(r -> r.field(field).gte(JsonData.of(value))));
        return this;
    }

    public EsQueryWrapper lt(String field, Object value) {
        bool.must(m -> m.range(r -> r.field(field).lt(JsonData.of(value))));
        return this;
    }

    public EsQueryWrapper le(String field, Object value) {
        if (value == null) {
            return this;
        }
        bool.must(m -> m.range(r -> r.field(field).lte(JsonData.of(value))));
        return this;
    }

    /**
     * should（OR）
     */
    public EsQueryWrapper shouldEq(String field, Object value) {
        bool.should(m -> m.term(t -> t.field(field).value(FieldValue.of(value))));
        return this;
    }

    public EsQueryWrapper minimumShouldMatch(int count) {
        bool.minimumShouldMatch(String.valueOf(count));
        return this;
    }

    /**
     * filter（不参与评分）
     */
    public EsQueryWrapper filterEq(String field, Object value) {
        bool.filter(m -> m.term(t -> t.field(field).value(FieldValue.of(value))));
        return this;
    }

    /**
     * nested
     */
    public EsQueryWrapper nested(String path, EsQueryWrapper wrapper) {
        bool.must(m -> m.nested(n -> n
            .path(path)
            .query(wrapper.bool.build()._toQuery())
        ));
        return this;
    }

    /**
     * 排序（安全版）
     */
    public EsQueryWrapper orderByDesc(String field) {
        return orderByDesc(field, false);
    }

    public EsQueryWrapper orderByDesc(String field, boolean keyword) {
        sorts.add(SortOptions.of(s -> s.field(f -> f
            .field(keyword ? field + FIELD_KEYWORD : field)
            .order(SortOrder.Desc)
        )));
        return this;
    }

    public EsQueryWrapper orderByAsc(String field) {
        return orderByAsc(field, false);
    }

    public EsQueryWrapper orderByAsc(String field, boolean keyword) {
        sorts.add(SortOptions.of(s -> s.field(f -> f
            .field(keyword ? field + FIELD_KEYWORD : field)
            .order(SortOrder.Asc)
        )));
        return this;
    }

    /**
     * limit
     */
    public EsQueryWrapper limit(int size) {
        this.size = size;
        return this;
    }

    /**
     * search_after（深分页）
     */
    public EsQueryWrapper searchAfter(List<Object> values) {
        this.searchAfter = values;
        return this;
    }

    /**
     * source 过滤
     */
    public EsQueryWrapper source(String[] includes, String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
        return this;
    }

    /**
     * max 聚合
     */
    public EsQueryWrapper max(String field) {
        this.aggregations.put(
            field,
            Aggregation.of(a -> a.max(m -> m.field(field)))
        );
        return this;
    }

    /**
     * 获取聚合
     */
    public Map<String, Aggregation> getAggregations() {
        return aggregations;
    }

    /**
     * 构建 NativeQueryBuilder
     */
    private NativeQueryBuilder createBuilder() {
        NativeQueryBuilder builder = NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withTrackTotalHits(true);

        // 排序
        if (CollUtil.isNotEmpty(sorts)) {
            builder.withSort(sorts);
        }

        // 聚合
        if (MapUtil.isNotEmpty(aggregations)) {
            aggregations.forEach(builder::withAggregation);
        }

        // search_after
        if (CollUtil.isNotEmpty(searchAfter)) {
            builder.withSearchAfter(searchAfter);
        }

        // source过滤
        if (includes != null || excludes != null) {
            builder.withSourceFilter(new SourceFilter() {
                @Override
                public String[] getIncludes() {
                    return includes;
                }

                @Override
                public String[] getExcludes() {
                    return excludes;
                }
            });
        }

        return builder;
    }

    /**
     * 普通查询
     */
    public Query build() {
        NativeQueryBuilder builder = createBuilder();

        builder.withMaxResults(
            size == null ? 1000 : size
        );

        return builder.build();
    }

    /**
     * MyBatis Plus分页.
     *
     * @param page page
     * @return Query
     */
    public Query build(IPage<?> page) {
        return build(buildPageable(page));
    }

    /**
     * Pageable分页，自动支持 search_after
     *
     * @param pageable pageable
     * @return Query
     */
    public Query build(Pageable pageable) {
        NativeQueryBuilder builder = createBuilder();
        if (CollUtil.isNotEmpty(searchAfter)) {
            // search_after 模式
            int pageSize = pageable == null
                ? 10 : pageable.getPageSize();
            builder.withMaxResults(pageSize);
        } else {
            // 普通分页
            builder.withPageable(pageable);
        }

        return builder.build();
    }

    /**
     * search_after专用分页.
     *
     * @param pageSize 每页条数
     * @return Query
     */
    public Query buildSearchAfter(int pageSize) {
        NativeQueryBuilder builder = createBuilder();

        builder.withMaxResults(pageSize);

        return builder.build();
    }

    /**
     * 获取BoolQuery
     *
     * @return BoolQuery
     */
    public BoolQuery getBool() {
        return this.bool.build();
    }

    /**
     * 获取BoolQuery.Builder
     *
     * @return BoolQuery.Builder
     */
    protected BoolQuery.Builder getBoolBuilder() {
        return this.bool;
    }

    /**
     * 是否为空
     *
     * @param b
     * @return boolean
     */
    public boolean isEmpty(BoolQuery b) {
        return (b.must() == null || b.must().isEmpty())
            && (b.should() == null || b.should().isEmpty())
            && (b.filter() == null || b.filter().isEmpty())
            && (b.mustNot() == null || b.mustNot().isEmpty());
    }

    /**
     * 构建分页
     *
     * @param page page
     * @return Pageable
     */
    public Pageable buildPageable(IPage<?> page) {
        if (page == null) {
            return PageRequest.of(0, 10);
        }

        List<Sort.Order> orders = new ArrayList<>(20);

        if (CollUtil.isEmpty(page.orders())) {
            orders.add(Sort.Order.desc(DbConstant.MODIFIED_TIMESTAMP));
        } else {
            for (OrderItem item : page.orders()) {
                orders.add(new Sort.Order(
                    item.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC,
                    item.getColumn()
                ));
            }
        }

        return PageRequest.of(
            (int) (page.getCurrent() - 1),
            (int) page.getSize(),
            Sort.by(orders)
        );
    }
}