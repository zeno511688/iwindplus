/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.iwindplus.base.async.task.dal.mapper.AsyncTaskMapper;
import com.iwindplus.base.async.task.dal.mapper.AsyncTaskSubMapper;
import com.iwindplus.base.async.task.dal.model.AsyncTaskDO;
import com.iwindplus.base.async.task.dal.model.AsyncTaskDO.AsyncTaskDOBuilder;
import com.iwindplus.base.async.task.dal.model.AsyncTaskSubDO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskExtDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskGrouSaveDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSaveDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskStatusEditDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubSaveDTO;
import com.iwindplus.base.async.task.domain.enums.AsyncTaskStatusEnum;
import com.iwindplus.base.async.task.domain.property.AsyncTaskProperty;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步任务聚合层.
 *
 * @author zengdegui
 * @since 2023/9/1
 */
@RequiredArgsConstructor
public class AsyncTaskRepository extends CrudRepository<AsyncTaskMapper, AsyncTaskDO> {

    private final AsyncTaskProperty property;
    private final AsyncTaskSubMapper asyncTaskSubMapper;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public AsyncTaskVO save(AsyncTaskSaveDTO entity) {
        final AsyncTaskDO model = this.buildAsyncTask(entity);
        this.save(model);
        return BeanUtil.copyProperties(model, AsyncTaskVO.class);
    }

    /**
     * 保持组任务.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public AsyncTaskVO saveGroup(AsyncTaskGrouSaveDTO entity) {
        final AsyncTaskVO result = this.save(entity);
        final List<AsyncTaskSubSaveDTO> subTasks = entity.getSubTasks();
        subTasks.forEach(subTask -> {
            subTask.setAsyncTaskId(result.getId());
            if (CharSequenceUtil.isBlank(subTask.getBizNumber())) {
                subTask.setBizNumber(IdWorker.getIdStr());
            }
        });
        final List<AsyncTaskSubDO> subEntities = BeanUtil.copyToList(subTasks, AsyncTaskSubDO.class);
        this.asyncTaskSubMapper.insert(subEntities);
        result.setSubTasks(BeanUtil.copyToList(subEntities, AsyncTaskSubVO.class));
        return result;
    }

    /**
     * 通过业务流水号删除.
     *
     * @param env       环境
     * @param bizNumber 业务流水号
     * @param deleted   是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByBizNumber(
        String env, String bizNumber, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteByBizNumber(env, bizNumber) > 0;
        }

        final List<Long> ids = super.listObjs(Wrappers.lambdaQuery(AsyncTaskDO.class)
                .eq(AsyncTaskDO::getEnv, env.trim())
                .eq(AsyncTaskDO::getBizNumber, bizNumber.trim())
                .last("LIMIT 1")
            , value -> Long.valueOf(value.toString()));
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        this.asyncTaskSubMapper.delete(Wrappers.lambdaQuery(AsyncTaskSubDO.class)
            .in(AsyncTaskSubDO::getAsyncTaskId, ids));
        return super.removeByIds(ids);
    }

    /**
     * 批量真实删除.
     *
     * @param ids     主键集合
     * @param deleted 是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteDataByIds(ids) > 0;
        }

        this.asyncTaskSubMapper.delete(Wrappers.lambdaQuery(AsyncTaskSubDO.class)
            .in(AsyncTaskSubDO::getAsyncTaskId, ids));
        return super.removeByIds(ids);
    }

    /**
     * 真实删除.
     *
     * @param id      主键
     * @param deleted 是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteDataById(id) > 0;
        }

        this.asyncTaskSubMapper.delete(Wrappers.lambdaQuery(AsyncTaskSubDO.class)
            .eq(AsyncTaskSubDO::getAsyncTaskId, id));
        return super.removeById(id);
    }

    /**
     * 修改.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AsyncTaskDO entity) {
        return super.updateById(entity);
    }

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(AsyncTaskStatusEditDTO entity) {
        final AsyncTaskDOBuilder<?, ?> builder = AsyncTaskDO.builder()
            .modifiedTimestamp(System.currentTimeMillis());
        if (Objects.nonNull(entity.getTo())) {
            builder.status(entity.getTo());
        }
        if (Objects.nonNull(entity.getCostTime())) {
            builder.costTime(entity.getCostTime());
        }
        if (CharSequenceUtil.isNotBlank(entity.getErrorMsg())) {
            builder.errorMsg(entity.getErrorMsg());
        }
        if (entity.getRetryCount() != null) {
            builder.retryCount(entity.getRetryCount());
        }
        if (Objects.nonNull(entity.getResult())) {
            builder.result(entity.getResult());
        }
        if (entity.getNextRetryTime() != null) {
            builder.nextRetryTime(entity.getNextRetryTime());
        }
        if (entity.getExpireTime() != null) {
            builder.expireTime(entity.getExpireTime());
        }
        if (entity.getProgress() != null) {
            builder.progress(entity.getProgress());
        }
        if (entity.getExt() != null) {
            builder.ext(entity.getExt());
        }

        final LambdaUpdateWrapper<AsyncTaskDO> updateWrapper = Wrappers.<AsyncTaskDO>lambdaUpdate()
            .eq(AsyncTaskDO::getId, entity.getId());
        if (entity.getFrom() != null) {
            updateWrapper.eq(AsyncTaskDO::getStatus, entity.getFrom());
        }

        return super.update(builder.build(), updateWrapper);
    }

    private AsyncTaskDO buildAsyncTask(AsyncTaskSaveDTO entity) {
        entity.setStatus(AsyncTaskStatusEnum.PENDING);
        entity.setEnv(SpringUtil.getActiveProfile());
        entity.setExpireTime(this.getNextExpireTime(System.currentTimeMillis()));
        entity.setNextRetryTime(System.currentTimeMillis());
        if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
            entity.setBizNumber(IdWorker.getIdStr());
        }
        if (MapUtil.isEmpty(entity.getParam())) {
            entity.setParam(MapUtil.newHashMap());
        }

        // 构建扩展配置，提交方未设置的字段使用系统配置
        final AsyncTaskExtDTO ext = Objects.isNull(entity.getExt())
            ? AsyncTaskExtDTO.builder().build() : entity.getExt();
        if (Objects.isNull(ext.getMaxAttempts())) {
            ext.setMaxAttempts(this.property.getRetry().getMaxAttempts());
        }
        if (Objects.isNull(ext.getEnabledUnlimitedRetry())) {
            ext.setEnabledUnlimitedRetry(this.property.getRetry().getEnabledUnlimitedRetry());
        }
        if (Objects.isNull(ext.getEnabledSuccessDelete())) {
            ext.setEnabledSuccessDelete(this.property.getEnabledSuccessDelete());
        }
        entity.setExt(ext);

        return BeanUtil.copyProperties(entity, AsyncTaskDO.class);
    }

    /**
     * 计算执行租约到期时间.
     *
     * @param baseTimeMillis 基准时间戳(毫秒)
     * @return long
     */
    public long getNextExpireTime(long baseTimeMillis) {
        return baseTimeMillis +
            Optional.ofNullable(this.property.getTimeoutSeconds()).orElse(60L) * NumberConstant.NUMBER_ONE_THOUSAND;
    }
}
