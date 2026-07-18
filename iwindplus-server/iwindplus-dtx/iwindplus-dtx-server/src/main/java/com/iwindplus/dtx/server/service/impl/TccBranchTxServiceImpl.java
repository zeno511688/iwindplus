/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import com.iwindplus.dtx.domain.dto.TccBranchTxSearchDTO;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import com.iwindplus.dtx.domain.vo.TccBranchTxPageVO;
import com.iwindplus.dtx.domain.vo.TccBranchTxVO;
import com.iwindplus.dtx.server.dal.model.TccBranchTxDO;
import com.iwindplus.dtx.server.dal.repository.TccBranchTxRepository;
import com.iwindplus.dtx.server.service.TccBranchTxService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API白名单业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class TccBranchTxServiceImpl implements TccBranchTxService {

    private final TccBranchTxRepository tccBranchTxRepository;

    @Override
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

    @Override
    public boolean removeByIds(List<Long> ids) {
        List<TccBranchTxDO> list = this.tccBranchTxRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.tccBranchTxRepository.getBaseMapper().deleteByIds(ids);
        return Boolean.TRUE;
    }

    @Override
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

    @Override
    public boolean editStatusById(Long id, BranchTxStatusEnum from, BranchTxStatusEnum to) {
        return this.tccBranchTxRepository.updateStatusById(id, from, to);
    }

    @Override
    public boolean editStatusById(Long id, BranchTxStatusEnum from, BranchTxStatusEnum to, String errorMsg) {
        return this.tccBranchTxRepository.updateStatusById(id, from, to, errorMsg);
    }

    @Override
    public boolean editStatusByMultiFrom(Long id, List<BranchTxStatusEnum> fromList, BranchTxStatusEnum to) {
        return this.tccBranchTxRepository.updateStatusByIdAndMultiFrom(id, fromList, to);
    }

    @Override
    public IPage<TccBranchTxPageVO> page(TccBranchTxSearchDTO entity) {
        PageDTO<TccBranchTxDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<TccBranchTxDO> queryWrapper = Wrappers.lambdaQuery(TccBranchTxDO.class)
            .orderByDesc(TccBranchTxDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(TccBranchTxDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getXid())) {
            queryWrapper.eq(TccBranchTxDO::getXid, entity.getXid().trim());
        }
        if (Objects.nonNull(entity.getBranchId())) {
            queryWrapper.eq(TccBranchTxDO::getBranchId, entity.getBranchId());
        }
        queryWrapper.select(TccBranchTxDO::getId, TccBranchTxDO::getCreatedTime, TccBranchTxDO::getCreatedTimestamp, TccBranchTxDO::getCreatedBy,
            TccBranchTxDO::getModifiedTime, TccBranchTxDO::getModifiedTimestamp, TccBranchTxDO::getModifiedBy, TccBranchTxDO::getVersion,
            TccBranchTxDO::getStatus, TccBranchTxDO::getXid, TccBranchTxDO::getBranchId, TccBranchTxDO::getContextPath, TccBranchTxDO::getConfirmUrl,
            TccBranchTxDO::getCancelUrl
        );
        final PageDTO<TccBranchTxDO> modelPage = this.tccBranchTxRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, TccBranchTxPageVO.class));
    }

    @Override
    public TccBranchTxVO getDetail(Long id) {
        TccBranchTxDO data = this.tccBranchTxRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, TccBranchTxVO.class);
    }

    @Override
    public List<TccBranchTxDO> listByXid(String xid, List<BranchTxStatusEnum> statusList) {
        return this.tccBranchTxRepository.listByXid(xid, statusList);
    }
}
