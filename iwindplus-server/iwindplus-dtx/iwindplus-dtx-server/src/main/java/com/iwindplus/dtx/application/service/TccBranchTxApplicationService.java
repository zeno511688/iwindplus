/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.api.dto.TccBranchTxDTO;
import com.iwindplus.dtx.common.enums.BranchTxStatusEnum;
import com.iwindplus.dtx.infrastructure.persistence.TccBranchTxDO;
import com.iwindplus.dtx.infrastructure.persistence.TccBranchTxRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tcc分支事务业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class TccBranchTxApplicationService {

    private final TccBranchTxRepository tccBranchTxRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(TccBranchTxDTO entity) {
        if (Objects.isNull(entity.getBranchId())) {
            entity.setBranchId(IdUtil.getSnowflakeNextId());
        }
        entity.setStatus(BranchTxStatusEnum.TRYING);
        final TccBranchTxDO model = BeanUtil.copyProperties(entity, TccBranchTxDO.class);
        this.tccBranchTxRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<Long> ids) {
        List<TccBranchTxDO> list = this.tccBranchTxRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.tccBranchTxRepository.getBaseMapper().deleteByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(TccBranchTxDTO entity) {
        TccBranchTxDO data = this.tccBranchTxRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final TccBranchTxDO model = BeanUtil.copyProperties(entity, TccBranchTxDO.class);
        this.tccBranchTxRepository.updateById(model);
        return Boolean.TRUE;
    }
}
