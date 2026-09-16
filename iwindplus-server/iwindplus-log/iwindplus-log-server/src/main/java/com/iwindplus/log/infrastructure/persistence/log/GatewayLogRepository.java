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
 * 网关日志查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:30
 */
@Repository
public class GatewayLogRepository extends EsBaseServiceImpl<GatewayLogDO> implements EsBaseService<GatewayLogDO> {

}
