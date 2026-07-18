/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.mongo.domain.MongoDbBaseDO;
import com.iwindplus.base.mongo.support.MongoLambdaQueryWrapper;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.core.query.Query;

/**
 * mongo业务层基础接口类.
 *
 * @param <T> 对象
 * @author zengdegui
 * @since 2019/8/9
 */
public interface MongoBaseService<T extends MongoDbBaseDO> {

    /**
     * 插入单个.
     *
     * @param entity 对象
     * @return T
     */
    T save(T entity);

    /**
     * 插入（批量）.
     *
     * @param entities 对象集合
     * @return boolean
     */
    default Collection<T> saveBatch(Collection<T> entities) {
        return this.saveBatch(entities, CommonConstant.NumberConstant.NUMBER_ONE_THOUSAND);
    }

    /**
     * 插入（批量）.
     *
     * @param entities  对象集合
     * @param batchSize 批次大小
     * @return boolean
     */
    Collection<T> saveBatch(Collection<T> entities, int batchSize);

    /**
     * 插入或修改（批量）.
     *
     * @param entities 对象集合
     * @return boolean
     */
    default boolean saveOrUpdateBatch(Collection<T> entities) {
        return this.saveOrUpdateBatch(entities, CommonConstant.NumberConstant.NUMBER_ONE_THOUSAND);
    }

    /**
     * 插入或修改（批量）.
     *
     * @param entities  对象集合
     * @param batchSize 批次大小
     * @return boolean
     */
    boolean saveOrUpdateBatch(Collection<T> entities, int batchSize);

    /**
     * 根据主键删除.
     *
     * @param id 主键
     * @return boolean
     */
    default boolean removeById(String id) {
        return this.removeById(id, Boolean.TRUE);
    }

    /**
     * 根据主键删除.
     *
     * @param id      主键
     * @param deleted 是否真删
     * @return boolean
     */
    default boolean removeById(String id, boolean deleted) {
        return this.removeByIds(Arrays.asList(id), deleted);
    }

    /**
     * 删除（批量）.
     *
     * @param ids 主键集合
     * @return boolean
     */
    default boolean removeByIds(Collection<?> ids) {
        return this.removeByIds(ids, Boolean.TRUE);
    }

    /**
     * 删除（批量）.
     *
     * @param ids     主键集合
     * @param deleted 是否真删
     * @return boolean
     */
    boolean removeByIds(Collection<?> ids, boolean deleted);

    /**
     * 删除.
     *
     * @param query 条件
     * @return boolean
     */
    default boolean remove(Query query) {
        return this.remove(query, Boolean.TRUE);
    }

    /**
     * Lambda 删除
     *
     * @param wrapper 条件
     * @return boolean
     */
    default boolean remove(MongoLambdaQueryWrapper<T> wrapper) {
        return this.remove(wrapper.build());
    }

    /**
     * 删除.
     *
     * @param query   条件
     * @param deleted 是否真删
     * @return boolean
     */
    boolean remove(Query query, boolean deleted);

    /**
     * Lambda 删除（逻辑/物理）
     *
     * @param wrapper 条件
     * @param deleted 是否真删
     * @return boolean
     */
    default boolean remove(MongoLambdaQueryWrapper<T> wrapper, boolean deleted) {
        return this.remove(wrapper.build(), deleted);
    }

    /**
     * 根据主键修改.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean updateById(T entity);

    /**
     * 根据条件修改.
     *
     * @param entity 对象
     * @param query  条件
     * @return boolean
     */
    boolean update(T entity, Query query);

    /**
     * Lambda 条件更新
     *
     * @param entity  更新对象
     * @param wrapper 条件
     * @return boolean
     */
    default boolean update(T entity, MongoLambdaQueryWrapper<T> wrapper) {
        return this.update(entity, wrapper.build());
    }

    /**
     * 修改（批量）.
     *
     * @param entities 对象
     * @return boolean
     */
    default boolean updateBatchById(Collection<T> entities) {
        return this.updateBatchById(entities, CommonConstant.NumberConstant.NUMBER_ONE_THOUSAND);
    }

    /**
     * 修改（批量）.
     *
     * @param entities  对象
     * @param batchSize 批次大小
     * @return boolean
     */
    boolean updateBatchById(Collection<T> entities, int batchSize);

    /**
     * 列表.
     *
     * @param page  分页
     * @param query 条件
     * @param <E>   泛型
     * @return <E extends IPage<T>>
     */
    <E extends IPage<T>> E page(E page, Query query);

    /**
     * Lambda 条件分页查询
     *
     * @param page    分页对象
     * @param wrapper 条件
     * @param <E>     分页类型
     * @return 分页结果
     */
    default <E extends IPage<T>> E page(E page, MongoLambdaQueryWrapper<T> wrapper) {
        return this.page(page, wrapper.build());
    }

    /**
     * 查询所有.
     *
     * @return List<T>
     */
    List<T> list();

    /**
     * 根据主键集合查询
     *
     * @param ids 主键集合
     * @return List<T>
     */
    List<T> listById(List<String> ids);

    /**
     * 查询所有.
     *
     * @param query 条件
     * @return List<T>
     */
    List<T> list(Query query);

    /**
     * Lambda 条件查询列表
     *
     * @param wrapper 条件
     * @return List<T>
     */
    default List<T> list(MongoLambdaQueryWrapper<T> wrapper) {
        return this.list(wrapper.build());
    }

    /**
     * 根据主键查询.
     *
     * @param id 主键
     * @return T
     */
    T getById(String id);

    /**
     * 查询单个.
     *
     * @param query 条件
     * @return T
     */
    T getOne(Query query);

    /**
     * Lambda 查询单个
     *
     * @param wrapper 条件
     * @return T
     */
    default T getOne(MongoLambdaQueryWrapper<T> wrapper) {
        return this.getOne(wrapper.build());
    }

    /**
     * 查询总数.
     *
     * @param query 条件
     * @return long
     */
    long count(Query query);

    /**
     * Lambda 查询数量
     *
     * @param wrapper 条件
     * @return long
     */
    default long count(MongoLambdaQueryWrapper<T> wrapper) {
        return this.count(wrapper.build());
    }

    /**
     * 是否存在
     *
     * @param wrapper 条件
     * @return boolean
     */
    boolean exists(MongoLambdaQueryWrapper<T> wrapper);
}
