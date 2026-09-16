/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.log.api.dto.BinlogAlertDTO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.BinlogAlertDO;
import com.iwindplus.log.infrastructure.persistence.log.BinlogAlertRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * binlog告警业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_BINLOG_ALERT})
@RequiredArgsConstructor
public class BinlogAlertApplicationService {

    private final BinlogAlertRepository binlogAlertRepository;

    /**
     * 保存
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean save(BinlogAlertDTO entity) {
        final BinlogAlertDO model = BeanUtil.copyProperties(entity, BinlogAlertDO.class);
        this.binlogAlertRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 批量保存
     *
     * @param entities 对象集合
     * @return boolean
     */
    public boolean saveBatch(List<BinlogAlertDTO> entities) {
        final List<BinlogAlertDO> models = BeanUtil.copyToList(entities, BinlogAlertDO.class);
        this.binlogAlertRepository.saveBatch(models);
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<String> ids) {
        List<BinlogAlertDO> data = this.binlogAlertRepository.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return this.binlogAlertRepository.removeByIds(ids, false);
    }
}
