/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.coordinator;

import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import java.time.LocalDateTime;

/**
 * tcc协调器.
 *
 * @author zengdegui
 * @since 2026/02/04 20:52
 */
public interface TccCoordinator {

    /**
     * 开启全局事务.
     *
     * @param bizType        业务类型
     * @param timeoutSeconds 超时时间
     * @return String
     */
    String begin(String bizType, Long timeoutSeconds);

    /**
     * 提交全局事务.
     *
     * @param xid 全局事务ID
     * @return boolean
     */
    boolean confirm(String xid);

    /**
     * 回滚全局事务.
     *
     * @param xid 全局事务ID
     * @return boolean
     */
    boolean cancel(String xid);

    /**
     * 注册分支事务.
     *
     * @param entity 分支事务对象
     * @return Long
     */
    Long register(TccBranchTxDTO entity);

    /**
     * 分支事务尝试成功.
     *
     * @param id 主键
     * @return boolean
     */
    boolean trySuccess(Long id);

    /**
     * 分支事务尝试失败.
     *
     * @param id 主键
     * @return boolean
     */
    boolean tryFail(Long id);

    /**
     * 获取每页条数.
     *
     * @return Integer
     */
    Integer getSize();

    /**
     * 获取下次重试时间.
     *
     * @param baseTime   基准时间
     * @param retryCount 重试次数
     * @return LocalDateTime
     */
    LocalDateTime getNextRetryTime(LocalDateTime baseTime, Integer retryCount);
}
