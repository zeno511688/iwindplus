/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.es.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.es.domain.EsDbBaseDO;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * es业务层基础接口类.
 *
 * @param <T> 对象
 * @author zengdegui
 * @since 2019/8/9
 */
public interface EsBaseService<T extends EsDbBaseDO> {

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
    default boolean removeByIds(Collection<? extends Serializable> ids) {
        return this.removeByIds(ids, Boolean.TRUE);
    }

    /**
     * 删除（批量）.
     *
     * @param ids     主键集合
     * @param deleted 是否真删
     * @return boolean
     */
    boolean removeByIds(Collection<? extends Serializable> ids, boolean deleted);

    /**
     * 删除.
     *
     * @param wrapper 条件
     * @return boolean
     */
    boolean remove(EsLambdaQueryWrapper<T> wrapper);

    /**
     * 根据主键修改.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean updateById(T entity);

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
     * @param page    分页
     * @param wrapper 条件
     * @param <E>     泛型
     * @return <E extends IPage<T>>
     */
    <E extends IPage<T>> E page(E page, EsLambdaQueryWrapper<T> wrapper);

    /**
     * 列表查询（深分页）
     *
     * @param page    分页
     * @param wrapper 条件
     * @return EsPageDTO<T>
     */
    EsPageDTO<T> pageByAfter(EsPageDTO<T> page, EsLambdaQueryWrapper<T> wrapper);

    /**
     * 列表查询（不分页）
     *
     * @param wrapper 条件
     * @return List<T>
     */
    List<T> list(EsLambdaQueryWrapper<T> wrapper);

    /**
     * 根据主键集合查询
     *
     * @param ids 主键集合
     * @return List<T>
     */
    List<T> listById(List<String> ids);

    /**
     * 单条查询
     *
     * @param wrapper 条件
     * @return boolean
     */
    T getOne(EsLambdaQueryWrapper<T> wrapper);

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return boolean
     */
    T getById(String id);

    /**
     * 数量统计
     *
     * @param wrapper 条件
     * @return boolean
     */
    long count(EsLambdaQueryWrapper<T> wrapper);

    /**
     * 是否存在
     *
     * @param wrapper 条件
     * @return boolean
     */
    boolean exists(EsLambdaQueryWrapper<T> wrapper);
}
