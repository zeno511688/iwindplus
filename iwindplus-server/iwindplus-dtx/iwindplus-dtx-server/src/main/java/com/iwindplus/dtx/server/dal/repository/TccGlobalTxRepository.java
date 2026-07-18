/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.dal.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.domain.enums.DtxCodeEnum;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.server.dal.mapper.TccBranchTxMapper;
import com.iwindplus.dtx.server.dal.mapper.TccGlobalTxMapper;
import com.iwindplus.dtx.server.dal.model.TccBranchTxDO;
import com.iwindplus.dtx.server.dal.model.TccGlobalTxDO;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * tcc全局事务聚合层接口类.
 *
 * @author zengdegui
 * @since 2026/02/04 20:55
 */
@Repository
public class TccGlobalTxRepository extends CrudRepository<TccGlobalTxMapper, TccGlobalTxDO> {

    @Resource
    private TccBranchTxMapper tccBranchTxMapper;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TccGlobalTxDO entity) {
        return super.save(entity);
    }

    /**
     * 真实删除.
     *
     * @param xid     全局事务ID
     * @param deleted 是否真删
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByXid(String xid, boolean deleted) {
        // 真实删除
        if (Boolean.TRUE.equals(deleted)) {
            final int globalDeleteFlag = super.baseMapper.deleteByXid(xid);
            final int branchDeleteFlag = this.tccBranchTxMapper.deleteByXid(xid);
            return globalDeleteFlag > 0 && branchDeleteFlag > 0;
        }
        final int globalDeleteFlag = super.baseMapper.delete(Wrappers.lambdaUpdate(TccGlobalTxDO.class)
            .eq(TccGlobalTxDO::getXid, xid));
        final int branchDeleteFlag = this.tccBranchTxMapper.delete(Wrappers.lambdaUpdate(TccBranchTxDO.class)
            .eq(TccBranchTxDO::getXid, xid));
        return globalDeleteFlag > 0 && branchDeleteFlag > 0;
    }

    /**
     * 修改.
     *
     * @param entity 对象
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TccGlobalTxDO entity) {
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
    public boolean updateStatusById(Long id, GlobalTxStatusEnum from, GlobalTxStatusEnum to) {
        TccGlobalTxDO update = TccGlobalTxDO
            .builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<TccGlobalTxDO> updateWrapper = Wrappers.<TccGlobalTxDO>lambdaUpdate()
            .eq(TccGlobalTxDO::getId, id)
            .eq(TccGlobalTxDO::getStatus, from);

        return super.update(update, updateWrapper);
    }

    /**
     * 通过全局事务ID修改状态.
     *
     * @param xid  全局事务ID
     * @param from 从状态
     * @param to   到状态
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusByXid(String xid, GlobalTxStatusEnum from, GlobalTxStatusEnum to) {
        TccGlobalTxDO update = TccGlobalTxDO
            .builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<TccGlobalTxDO> updateWrapper = Wrappers.<TccGlobalTxDO>lambdaUpdate()
            .eq(TccGlobalTxDO::getXid, xid)
            .eq(TccGlobalTxDO::getStatus, from);

        return super.update(update, updateWrapper);
    }

    /**
     * 通过全局事务ID修改状态.
     *
     * @param xid           全局事务ID
     * @param from          从状态
     * @param to            到状态
     * @param retryCount    重试次数
     * @param nextRetryTime 下次重试时间
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusById(String xid, GlobalTxStatusEnum from, GlobalTxStatusEnum to,
        Integer retryCount, LocalDateTime nextRetryTime) {
        TccGlobalTxDO update = TccGlobalTxDO
            .builder()
            .status(to)
            .retryCount(retryCount)
            .nextRetryTime(nextRetryTime)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<TccGlobalTxDO> updateWrapper = Wrappers.<TccGlobalTxDO>lambdaUpdate()
            .eq(TccGlobalTxDO::getXid, xid)
            .eq(TccGlobalTxDO::getStatus, from);

        return super.update(update, updateWrapper);
    }

    /**
     * 通过全局事务ID修改状态.
     *
     * @param xid      全局事务ID
     * @param fromList 从状态集合
     * @param to       到状态
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusByXidAndMultiFrom(String xid, List<GlobalTxStatusEnum> fromList, GlobalTxStatusEnum to) {
        TccGlobalTxDO update = TccGlobalTxDO
            .builder()
            .status(to)
            .modifiedTime(LocalDateTime.now())
            .modifiedTimestamp(System.currentTimeMillis())
            .build();

        final LambdaUpdateWrapper<TccGlobalTxDO> updateWrapper = Wrappers.<TccGlobalTxDO>lambdaUpdate()
            .eq(TccGlobalTxDO::getXid, xid)
            .in(TccGlobalTxDO::getStatus, fromList);

        return super.update(update, updateWrapper);
    }

    /**
     * 检查全局事务ID是否存在.
     *
     * @param xid 全局事务ID
     */
    public void getXidExist(String xid) {
        final LambdaQueryWrapper<TccGlobalTxDO> queryWrapper = Wrappers.lambdaQuery(TccGlobalTxDO.class)
            .eq(TccGlobalTxDO::getXid, xid);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(DtxCodeEnum.XID_EXIST);
        }
    }

}
