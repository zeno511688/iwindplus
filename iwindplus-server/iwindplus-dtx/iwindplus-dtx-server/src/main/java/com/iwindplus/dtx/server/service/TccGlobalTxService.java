/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.dtx.domain.dto.TccGlobalTxDTO;
import com.iwindplus.dtx.domain.dto.TccGlobalTxSearchDTO;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.domain.vo.TccGlobalTxPageVO;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * tcc全局事务业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface TccGlobalTxService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(TccGlobalTxDTO entity);

    /**
     * 通过全局事务ID删除.
     *
     * @param xid     主键
     * @param deleted 是否真删
     * @return boolean
     */
    boolean removeByXid(String xid, boolean deleted);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(TccGlobalTxDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, GlobalTxStatusEnum status);

    /**
     * 通过主键修改状态.
     *
     * @param id   主键
     * @param from 从状态
     * @param to   到状态
     * @return boolean
     */
    boolean editStatusById(Long id, GlobalTxStatusEnum from, GlobalTxStatusEnum to);

    /**
     * 通过全局事务ID修改状态.
     *
     * @param xid  全局事务ID
     * @param from 从状态
     * @param to   到状态
     * @return boolean
     */
    boolean editStatusByXid(String xid, GlobalTxStatusEnum from, GlobalTxStatusEnum to);

    /**
     * 通过全局事务ID修改状态（乐观锁）.
     *
     * @param xid      全局事务ID
     * @param fromList 从状态
     * @param to       到状态
     * @return 更新成功返回最新的事务信息，失败返回null
     */
    TccGlobalTxVO editStatusByMultiFromWithResult(String xid, List<GlobalTxStatusEnum> fromList, GlobalTxStatusEnum to);

    /**
     * 通过全局事务ID修改状态.
     *
     * @param xid      全局事务ID
     * @param fromList 从状态
     * @param to       到状态
     * @return boolean
     */
    boolean editStatusByMultiFrom(String xid, List<GlobalTxStatusEnum> fromList, GlobalTxStatusEnum to);

    /**
     * 通过全局事务ID修改状态.
     *
     * @param xid           全局事务ID
     * @param from          从状态
     * @param to            到状态
     * @param retryCount    重试次数
     * @param nextRetryTime 下一次重试时间
     * @return boolean
     */
    boolean editStatusById(String xid, GlobalTxStatusEnum from, GlobalTxStatusEnum to, Integer retryCount, LocalDateTime nextRetryTime);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<TccGlobalTxPageVO>
     */
    IPage<TccGlobalTxPageVO> page(TccGlobalTxSearchDTO entity);

    /**
     * 详情.
     *
     * @param xid 全局事务ID
     * @return TccGlobalTxVO
     */
    TccGlobalTxVO getDetailByXid(String xid);

    /**
     * 详情.
     *
     * @param id 主键
     * @return TccGlobalTxVO
     */
    TccGlobalTxVO getDetail(Long id);
}
