/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdMapper;
import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdSubMapper;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO.AsyncCmdDOBuilder;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGrouSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdStatusEditDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubSaveDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步命令聚合层接口类.
 *
 * @author zengdegui
 * @since 2023/9/1
 */
@RequiredArgsConstructor
public class AsyncCmdRepository extends CrudRepository<AsyncCmdMapper, AsyncCmdDO> {

    private final AsyncCmdProperty property;
    private final AsyncCmdSubMapper asyncCmdSubMapper;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public AsyncCmdVO save(AsyncCmdSaveDTO entity) {
        final AsyncCmdDO model = this.buildAsyncCmd(entity);
        this.save(model);
        return BeanUtil.copyProperties(model, AsyncCmdVO.class);
    }

    /**
     * 保持组任务.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public AsyncCmdVO saveGroup(AsyncCmdGrouSaveDTO entity) {
        final AsyncCmdVO result = this.save(entity);
        final List<AsyncCmdSubSaveDTO> subTasks = entity.getSubTasks();
        subTasks.forEach(subTask -> {
            subTask.setAsyncCmdId(result.getId());
            if (CharSequenceUtil.isBlank(subTask.getBizNumber())) {
                subTask.setBizNumber(IdWorker.getIdStr());
            }
        });
        final List<AsyncCmdSubDO> subEntities = BeanUtil.copyToList(subTasks, AsyncCmdSubDO.class);
        this.asyncCmdSubMapper.insert(subEntities);
        result.setSubTasks(BeanUtil.copyToList(subEntities, AsyncCmdSubVO.class));
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

        final List<Long> ids = super.listObjs(Wrappers.lambdaQuery(AsyncCmdDO.class)
                .eq(AsyncCmdDO::getEnv, env.trim())
                .eq(AsyncCmdDO::getBizNumber, bizNumber.trim())
                .last("LIMIT 1")
            , value -> Long.valueOf(value.toString()));
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        this.asyncCmdSubMapper.delete(Wrappers.lambdaQuery(AsyncCmdSubDO.class)
            .in(AsyncCmdSubDO::getAsyncCmdId, ids));
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

        this.asyncCmdSubMapper.delete(Wrappers.lambdaQuery(AsyncCmdSubDO.class)
            .in(AsyncCmdSubDO::getAsyncCmdId, ids));
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

        this.asyncCmdSubMapper.delete(Wrappers.lambdaQuery(AsyncCmdSubDO.class)
            .eq(AsyncCmdSubDO::getAsyncCmdId, id));
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
    public boolean updateById(AsyncCmdDO entity) {
        return super.updateById(entity);
    }

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(AsyncCmdStatusEditDTO entity) {
        final AsyncCmdDOBuilder<?, ?> builder = AsyncCmdDO.builder()
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

        final LambdaUpdateWrapper<AsyncCmdDO> updateWrapper = Wrappers.<AsyncCmdDO>lambdaUpdate()
            .eq(AsyncCmdDO::getId, entity.getId());
        if (entity.getFrom() != null) {
            updateWrapper.eq(AsyncCmdDO::getStatus, entity.getFrom());
        }

        return super.update(builder.build(), updateWrapper);
    }

    /**
     * 编辑续订租期时间.
     *
     * @param id             主键
     * @param baseTimeMillis 基准时间戳(毫秒)
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editExpireTime(Long id, long baseTimeMillis) {
        final AsyncCmdDO entity = AsyncCmdDO.builder()
            .id(id)
            .expireTime(this.getNextExpireTime(baseTimeMillis))
            .modifiedTimestamp(System.currentTimeMillis())
            .build();
        return super.updateById(entity);
    }

    private AsyncCmdDO buildAsyncCmd(AsyncCmdSaveDTO entity) {
        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        entity.setDispatchMode(DispatchModeEnum.ASYNC);
        entity.setEnv(SpringUtil.getActiveProfile());
        entity.setExpireTime(this.getNextExpireTime(System.currentTimeMillis()));
        entity.setNextRetryTime(System.currentTimeMillis());
        if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
            entity.setBizNumber(IdWorker.getIdStr());
        }
        if (MapUtil.isEmpty(entity.getParam())) {
            entity.setParam(MapUtil.newHashMap());
        }
        return BeanUtil.copyProperties(entity, AsyncCmdDO.class);
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
