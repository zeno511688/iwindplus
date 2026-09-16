/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.application.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.application.query.vo.TccBranchTxPageVO;
import com.iwindplus.dtx.application.query.vo.TccBranchTxVO;
import com.iwindplus.dtx.application.query.dto.TccBranchTxSearchDTO;
import com.iwindplus.dtx.common.enums.BranchTxStatusEnum;
import com.iwindplus.dtx.infrastructure.persistence.TccBranchTxDO;
import com.iwindplus.dtx.infrastructure.persistence.TccBranchTxRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * tcc分支事务查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TccBranchTxQueryService {

    private final TccBranchTxRepository tccBranchTxRepository;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<TccBranchTxPageVO>
     */
    public IPage<TccBranchTxPageVO> page(TccBranchTxSearchDTO entity) {
        PageDTO<TccBranchTxDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<TccBranchTxDO> queryWrapper = Wrappers.lambdaQuery(TccBranchTxDO.class)
            .orderByDesc(TccBranchTxDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(TccBranchTxDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getXid())) {
            queryWrapper.eq(TccBranchTxDO::getXid, entity.getXid().trim());
        }
        if (Objects.nonNull(entity.getBranchId())) {
            queryWrapper.eq(TccBranchTxDO::getBranchId, entity.getBranchId());
        }
        queryWrapper.select(TccBranchTxDO::getId, TccBranchTxDO::getCreatedTimestamp, TccBranchTxDO::getCreatedBy,
            TccBranchTxDO::getModifiedTimestamp, TccBranchTxDO::getModifiedBy, TccBranchTxDO::getVersion,
            TccBranchTxDO::getStatus, TccBranchTxDO::getXid, TccBranchTxDO::getBranchId, TccBranchTxDO::getContextPath, TccBranchTxDO::getConfirmUrl,
            TccBranchTxDO::getCancelUrl
        );
        final PageDTO<TccBranchTxDO> modelPage = this.tccBranchTxRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, TccBranchTxPageVO.class));
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return TccBranchTxVO
     */
    public TccBranchTxVO getDetail(Long id) {
        TccBranchTxDO data = this.tccBranchTxRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, TccBranchTxVO.class);
    }

    /**
     * 通过全局事务ID查询.
     *
     * @param xid        全局事务ID
     * @param statusList 状态集合
     * @return List<TccBranchTxDO>
     */
    public List<TccBranchTxDO> listByXid(String xid, List<BranchTxStatusEnum> statusList) {
        return this.tccBranchTxRepository.listByXid(xid, statusList);
    }
}
