/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.support.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.dtx.domain.dto.TccGlobalTxShardSearchDTO;
import com.iwindplus.dtx.domain.dto.TccGlobalTxShardSearchDTO.TccGlobalTxShardSearchDTOBuilder;
import com.iwindplus.dtx.domain.enums.DtxJobEnum;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import com.iwindplus.dtx.domain.vo.TccGlobalTxVO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 分布式事务重试job操作策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Component
@Slf4j
public class DtxJobHandlerHandlerRetry extends AbstractDtxJobHandler {

    @Override
    public DtxJobEnum support() {
        return DtxJobEnum.RETRY_JOB;
    }

    @Override
    protected boolean doExecute(List<TccGlobalTxVO> entityList) {
        log.info("重试任务，size={}", entityList.size());
        if (CollUtil.isEmpty(entityList)) {
            return true;
        }
        for (TccGlobalTxVO tx : entityList) {
            if (tx.getStatus() == GlobalTxStatusEnum.CONFIRMING
                || tx.getStatus() == GlobalTxStatusEnum.CONFIRM_FAIL) {
                this.tccCoordinator.confirm(tx.getXid());
            }

            if (tx.getStatus() == GlobalTxStatusEnum.CANCELING
                || tx.getStatus() == GlobalTxStatusEnum.CANCEL_FAIL) {
                this.tccCoordinator.cancel(tx.getXid());
            }
        }
        return true;
    }

    @Override
    protected TccGlobalTxShardSearchDTO buildJobSearchDTO() {
        final TccGlobalTxShardSearchDTOBuilder<?, ?> builder = TccGlobalTxShardSearchDTO.builder()
            .statusList(GlobalTxStatusEnum.getRetryStatus())
            .retryTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(this.property.getRetry().getEnabledUnlimitedRetry())) {
            builder.retryCount(this.property.getRetry().getMaxAttempts());
        }

        return builder.build();
    }

}
