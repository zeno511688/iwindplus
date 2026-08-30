/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.dal.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.iwindplus.base.export.task.dal.mapper.ExportTaskMapper;
import com.iwindplus.base.export.task.dal.model.ExportTaskDO;
import com.iwindplus.base.export.task.dal.model.ExportTaskDO.ExportTaskDOBuilder;
import com.iwindplus.base.export.task.domain.dto.ExportTaskDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskExtDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 导出任务Repository.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@RequiredArgsConstructor
public class ExportTaskRepository extends CrudRepository<ExportTaskMapper, ExportTaskDO> {

    private final ExportTaskProperty property;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public ExportTaskVO save(ExportTaskDTO entity) {
        final ExportTaskDO model = this.buildExportTask(entity);
        this.save(model);
        return BeanUtil.copyProperties(model, ExportTaskVO.class);
    }

    /**
     * 修改.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(ExportTaskDO entity) {
        return super.updateById(entity);
    }

    /**
     * 通过主键修改状态.
     *
     * @param entity 状态流转对象（空字段不更新）
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(ExportTaskStatusEditDTO entity) {
        final ExportTaskDOBuilder<?, ?> builder = ExportTaskDO.builder()
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
        if (entity.getNextRetryTime() != null) {
            builder.nextRetryTime(entity.getNextRetryTime());
        }
        if (entity.getExpireTime() != null) {
            builder.expireTime(entity.getExpireTime());
        }
        if (entity.getProgress() != null) {
            builder.progress(entity.getProgress());
        }
        if (entity.getExportedCount() != null) {
            builder.exportedCount(entity.getExportedCount());
        }
        if (entity.getExt() != null) {
            builder.ext(entity.getExt());
        }

        final LambdaUpdateWrapper<ExportTaskDO> updateWrapper = Wrappers.<ExportTaskDO>lambdaUpdate()
            .eq(ExportTaskDO::getId, entity.getId());
        if (entity.getFrom() != null) {
            updateWrapper.eq(ExportTaskDO::getStatus, entity.getFrom());
        }

        return super.update(builder.build(), updateWrapper);
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

    private ExportTaskDO buildExportTask(ExportTaskDTO entity) {
        entity.setStatus(ExportTaskStatusEnum.PENDING);
        entity.setExpireTime(this.getNextExpireTime(System.currentTimeMillis()));
        entity.setNextRetryTime(System.currentTimeMillis());
        if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
            entity.setBizNumber(IdWorker.getIdStr());
        }
        if (MapUtil.isEmpty(entity.getQueryParam())) {
            entity.setQueryParam(MapUtil.newHashMap());
        }

        // 构建扩展配置，提交方未设置的字段使用系统配置
        final ExportTaskExtDTO ext = Objects.isNull(entity.getExt())
            ? ExportTaskExtDTO.builder().build() : entity.getExt();
        if (Objects.isNull(ext.getMaxAttempts())) {
            ext.setMaxAttempts(this.property.getRetry().getMaxAttempts());
        }
        if (Objects.isNull(ext.getEnabledUnlimitedRetry())) {
            ext.setEnabledUnlimitedRetry(this.property.getRetry().getEnabledUnlimitedRetry());
        }
        entity.setExt(ext);

        return BeanUtil.copyProperties(entity, ExportTaskDO.class);
    }
}
