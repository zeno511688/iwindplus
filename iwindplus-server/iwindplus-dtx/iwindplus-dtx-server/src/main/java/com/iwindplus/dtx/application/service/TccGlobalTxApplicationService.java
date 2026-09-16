/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.api.dto.TccGlobalTxDTO;
import com.iwindplus.dtx.common.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.infrastructure.persistence.TccGlobalTxDO;
import com.iwindplus.dtx.infrastructure.persistence.TccGlobalTxRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tcc全局事务业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class TccGlobalTxApplicationService {

    private final TccGlobalTxRepository tccGlobalTxRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(TccGlobalTxDTO entity) {
        entity.setStatus(GlobalTxStatusEnum.TRYING);
        this.tccGlobalTxRepository.getXidExist(entity.getXid());
        entity.setEnv(SpringUtil.getActiveProfile());
        final TccGlobalTxDO model = BeanUtil.copyProperties(entity, TccGlobalTxDO.class);
        this.tccGlobalTxRepository.save(model);
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
        List<TccGlobalTxDO> list = this.tccGlobalTxRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.tccGlobalTxRepository.getBaseMapper().deleteByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(TccGlobalTxDTO entity) {
        TccGlobalTxDO data = this.tccGlobalTxRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getXid()) && !CharSequenceUtil.equals(data.getXid(), entity.getXid().trim())) {
            this.tccGlobalTxRepository.getXidExist(entity.getXid().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final TccGlobalTxDO model = BeanUtil.copyProperties(entity, TccGlobalTxDO.class);
        this.tccGlobalTxRepository.updateById(model);
        return Boolean.TRUE;
    }
}
