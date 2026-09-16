/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.infrastructure.persistence.log;

import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import org.springframework.stereotype.Repository;

/**
 * 操作日志查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:35
 */
@Repository
public class OperationLogRepository extends EsBaseServiceImpl<OperationLogDO> implements EsBaseService<OperationLogDO> {

}
