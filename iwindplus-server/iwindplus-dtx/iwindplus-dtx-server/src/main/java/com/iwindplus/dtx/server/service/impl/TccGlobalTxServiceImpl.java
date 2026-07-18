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
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.domain.dto.TccGlobalTxDTO;
import com.iwindplus.dtx.domain.dto.TccGlobalTxSearchDTO;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.domain.vo.TccGlobalTxPageVO;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import com.iwindplus.dtx.server.dal.model.TccGlobalTxDO;
import com.iwindplus.dtx.server.dal.repository.TccGlobalTxRepository;
import com.iwindplus.dtx.server.service.TccGlobalTxService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * tcc全局事务业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class TccGlobalTxServiceImpl implements TccGlobalTxService {

    private final TccGlobalTxRepository tccGlobalTxRepository;

    @Override
    public boolean save(TccGlobalTxDTO entity) {
        entity.setStatus(GlobalTxStatusEnum.TRYING);
        this.tccGlobalTxRepository.getXidExist(entity.getXid());
        entity.setEnv(SpringUtil.getActiveProfile());
        final TccGlobalTxDO model = BeanUtil.copyProperties(entity, TccGlobalTxDO.class);
        this.tccGlobalTxRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean removeByXid(String xid, boolean deleted) {
        return this.tccGlobalTxRepository.deleteByXid(xid, deleted);
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        List<TccGlobalTxDO> list = this.tccGlobalTxRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.tccGlobalTxRepository.getBaseMapper().deleteByIds(ids);
        return Boolean.TRUE;
    }

    @Override
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

    @Override
    public boolean editStatus(Long id, GlobalTxStatusEnum status) {
        TccGlobalTxDO data = this.tccGlobalTxRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        TccGlobalTxDO param = new TccGlobalTxDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.tccGlobalTxRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public boolean editStatusById(Long id, GlobalTxStatusEnum from, GlobalTxStatusEnum to) {
        return this.tccGlobalTxRepository.updateStatusById(id, from, to);
    }

    @Override
    public boolean editStatusByXid(String xid, GlobalTxStatusEnum from, GlobalTxStatusEnum to) {
        return this.tccGlobalTxRepository.updateStatusByXid(xid, from, to);
    }

    @Override
    public boolean editStatusByMultiFrom(String xid, List<GlobalTxStatusEnum> fromList, GlobalTxStatusEnum to) {
        TccGlobalTxVO result = this.editStatusByMultiFromWithResult(xid, fromList, to);
        return result != null;
    }

    @Override
    public TccGlobalTxVO editStatusByMultiFromWithResult(String xid, List<GlobalTxStatusEnum> fromList, GlobalTxStatusEnum to) {
        boolean result = this.tccGlobalTxRepository.updateStatusByXidAndMultiFrom(xid, fromList, to);
        if (result) {
            // 更新成功后立即查询最新状态返回
            return this.getDetailByXid(xid);
        }
        return null;
    }

    @Override
    public boolean editStatusById(String xid, GlobalTxStatusEnum from, GlobalTxStatusEnum to, Integer retryCount,
        LocalDateTime nextRetryTime) {
        return this.tccGlobalTxRepository.updateStatusById(xid, from, to, retryCount, nextRetryTime);
    }

    @Override
    public IPage<TccGlobalTxPageVO> page(TccGlobalTxSearchDTO entity) {
        PageDTO<TccGlobalTxDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<TccGlobalTxDO> queryWrapper = Wrappers.lambdaQuery(TccGlobalTxDO.class)
            .orderByDesc(TccGlobalTxDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(TccGlobalTxDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getXid())) {
            queryWrapper.eq(TccGlobalTxDO::getXid, entity.getXid().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizType())) {
            queryWrapper.eq(TccGlobalTxDO::getBizType, entity.getBizType().trim());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(TccGlobalTxDO::getStatus, entity.getStatusList());
        }
        if (Objects.nonNull(entity.getRetryTime())) {
            queryWrapper.le(TccGlobalTxDO::getNextRetryTime, entity.getRetryTime());
        }
        if (Objects.nonNull(entity.getRetryCount())) {
            queryWrapper.le(TccGlobalTxDO::getRetryCount, entity.getRetryCount());
        }
        if (Objects.nonNull(entity.getExpireTime())) {
            queryWrapper.lt(TccGlobalTxDO::getExpireTime, entity.getExpireTime());
        }
        final PageDTO<TccGlobalTxDO> modelPage = this.tccGlobalTxRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, TccGlobalTxPageVO.class));
    }

    @Override
    public TccGlobalTxVO getDetailByXid(String xid) {
        TccGlobalTxDO data = this.tccGlobalTxRepository.getOne(Wrappers.lambdaQuery(TccGlobalTxDO.class)
            .eq(TccGlobalTxDO::getXid, xid));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, TccGlobalTxVO.class);
    }

    @Override
    public TccGlobalTxVO getDetail(Long id) {
        TccGlobalTxDO data = this.tccGlobalTxRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, TccGlobalTxVO.class);
    }
}
