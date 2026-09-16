/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.iwindplus.dtx.common.enums.BranchTxStatusEnum;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * tcc分支事务聚合层接口类.
 *
 * @author zengdegui
 * @since 2026/02/04 20:55
 */
@Repository
public class TccBranchTxRepository extends CrudRepository<TccBranchTxMapper, TccBranchTxDO> {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TccBranchTxDO entity) {
        return super.save(entity);
    }

    /**
     * 修改.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TccBranchTxDO entity) {
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
    public boolean updateStatusById(Long id, BranchTxStatusEnum from, BranchTxStatusEnum to) {
        return this.updateStatus(Wrappers.<TccBranchTxDO>lambdaUpdate()
            .eq(TccBranchTxDO::getId, id)
            .eq(TccBranchTxDO::getStatus, from), to, null);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id       主键
     * @param from     从状态
     * @param to       到状态
     * @param errorMsg 错误信息
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(Long id, BranchTxStatusEnum from, BranchTxStatusEnum to, String errorMsg) {
        return this.updateStatus(Wrappers.<TccBranchTxDO>lambdaUpdate()
            .eq(TccBranchTxDO::getId, id)
            .eq(TccBranchTxDO::getStatus, from), to, errorMsg);
    }

    /**
     * 通过主键修改状态.
     *
     * @param id       主键
     * @param fromList 从状态集合
     * @param to       到状态
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusByIdAndMultiFrom(Long id, List<BranchTxStatusEnum> fromList, BranchTxStatusEnum to) {
        return this.updateStatus(Wrappers.<TccBranchTxDO>lambdaUpdate()
            .eq(TccBranchTxDO::getId, id)
            .in(TccBranchTxDO::getStatus, fromList), to, null);
    }

    /**
     * 修改状态.
     *
     * @param updateWrapper 更新条件
     * @param to            到状态
     * @param errorMsg      错误信息
     * @return boolean
     */
    private boolean updateStatus(LambdaUpdateWrapper<TccBranchTxDO> updateWrapper, BranchTxStatusEnum to, String errorMsg) {
        final TccBranchTxDO update = TccBranchTxDO
            .builder()
            .status(to)
            .errorMsg(errorMsg)
            .modifiedTimestamp(System.currentTimeMillis())
            .build();
        return super.update(update, updateWrapper);
    }

    /**
     * 根据全局事务ID查询分支事务列表.
     *
     * @param xid        全局事务ID
     * @param statusList 状态集合
     * @return List<TccBranchTxDO>
     */
    public List<TccBranchTxDO> listByXid(String xid, List<BranchTxStatusEnum> statusList) {
        return super.list(Wrappers.
            lambdaQuery(TccBranchTxDO.class)
            .eq(TccBranchTxDO::getXid, xid)
            .in(TccBranchTxDO::getStatus, statusList));
    }

}
