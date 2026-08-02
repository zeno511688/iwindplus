/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdMapper;
import com.iwindplus.base.async.cmd.dal.mapper.AsyncCmdSubMapper;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdDO.AsyncCmdDOBuilder;
import com.iwindplus.base.async.cmd.dal.model.AsyncCmdSubDO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGrouSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSubSaveDTO;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdStatusEnum;
import com.iwindplus.base.async.cmd.domain.enums.DispatchModeEnum;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.LocalDateTime;
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
        subTasks.forEach(subTask -> subTask.setAsyncCmdId(result.getId()));
        final List<AsyncCmdSubDO> subEntities = BeanUtil.copyToList(subTasks, AsyncCmdSubDO.class);
        final Long count = this.asyncCmdSubMapper.selectCount(Wrappers.lambdaQuery(AsyncCmdSubDO.class)
            .eq(AsyncCmdSubDO::getAsyncCmdId, result.getId()));
        if (count > 0) {
            throw new BizException(BizCodeEnum.DATA_EXIST);
        }
        this.asyncCmdSubMapper.insert(subEntities);
        return result;
    }

    /**
     * 通过环境和业务流水号删除.
     *
     * @param env       环境
     * @param bizNumber 业务流水号
     * @param deleted   是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByCondition(String env, String bizNumber, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            return super.baseMapper.deleteByBizNumber(env, bizNumber) > 0;
        }

        final AsyncCmdDO result = super.getOne(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, env.trim())
            .eq(AsyncCmdDO::getBizNumber, bizNumber.trim()));
        if (Objects.nonNull(result)) {
            final Long id = result.getId();
            this.asyncCmdSubMapper.delete(Wrappers.lambdaQuery(AsyncCmdSubDO.class)
                .eq(AsyncCmdSubDO::getAsyncCmdId, id));
            return super.removeById(id);
        }

        return super.remove(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getEnv, env.trim())
            .eq(AsyncCmdDO::getBizNumber, bizNumber.trim()));
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
     * @param id   主键
     * @param from 从状态
     * @param to   到状态
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to) {
        return updateStatusById(id, from, to, false);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id        主键
     * @param from      从状态
     * @param to        到状态
     * @param renewFlag 是否重置续约时间
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to, boolean renewFlag) {
        return updateStatusById(id, from, to, null, null, null, renewFlag);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id            主键
     * @param from          从状态
     * @param to            到状态
     * @param errorMsg      错误信息
     * @param retryCount    重试次数
     * @param nextRetryTime 下次重试时间
     * @param renewFlag     是否重置续约时间
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, AsyncCmdStatusEnum from, AsyncCmdStatusEnum to
        , String errorMsg, Integer retryCount, LocalDateTime nextRetryTime, boolean renewFlag) {
        final AsyncCmdDOBuilder<?, ?> builder = AsyncCmdDO.builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis());
        if (CharSequenceUtil.isNotBlank(errorMsg)) {
            builder.errorMsg(errorMsg);
        }
        if (retryCount != null) {
            builder.retryCount(retryCount);
        }
        if (nextRetryTime != null) {
            builder.nextRetryTime(nextRetryTime);
        }
        // 任务失败时，释放执行租约，任务成功时，不释放执行租约，任务执行中时，不释放执行租约
        if (AsyncCmdStatusEnum.FAILED == to) {
            builder.expireTime(LocalDateTime.now());
        }
        if (renewFlag) {
            builder.expireTime(this.getNextExpireTime());
        }

        final LambdaUpdateWrapper<AsyncCmdDO> updateWrapper = Wrappers.<AsyncCmdDO>lambdaUpdate()
            .eq(AsyncCmdDO::getId, id);
        if (from != null) {
            updateWrapper.eq(AsyncCmdDO::getStatus, from);
        }

        return super.update(builder.build(), updateWrapper);
    }

    /**
     * 编辑续订租期时间.
     *
     * @param id 主键
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editExpireTime(Long id) {
        final AsyncCmdDO entity = AsyncCmdDO.builder()
            .id(id)
            .expireTime(this.getNextExpireTime())
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();
        return super.updateById(entity);
    }

    /**
     * 获取业务流水号是否已存在.
     *
     * @param bizNumber 业务流水号
     */
    public void getBizNumberIsExist(String bizNumber) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(AsyncCmdDO.class)
            .eq(AsyncCmdDO::getBizNumber, bizNumber)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(BizCodeEnum.BIZ_NUMBER_EXIST, new Object[]{bizNumber});
        }
    }

    private AsyncCmdDO buildAsyncCmd(AsyncCmdSaveDTO entity) {
        entity.setStatus(AsyncCmdStatusEnum.TO_BE_EXECUTE);
        entity.setDispatchMode(DispatchModeEnum.ASYNC);
        entity.setEnv(SpringUtil.getActiveProfile());
        entity.setExpireTime(this.getNextExpireTime());
        entity.setNextRetryTime(LocalDateTime.now());
        if (MapUtil.isEmpty(entity.getContent())) {
            entity.setContent(MapUtil.newHashMap());
        }
        this.getBizNumberIsExist(entity.getBizNumber().trim());
        return BeanUtil.copyProperties(entity, AsyncCmdDO.class);
    }

    /**
     * 计算执行租约到期时间.
     *
     * @return LocalDateTime
     */
    public LocalDateTime getNextExpireTime() {
        return LocalDateTime.now()
            .plusSeconds(
                Optional.ofNullable(this.property.getTimeoutSeconds()).orElse(60L)
            );
    }
}
