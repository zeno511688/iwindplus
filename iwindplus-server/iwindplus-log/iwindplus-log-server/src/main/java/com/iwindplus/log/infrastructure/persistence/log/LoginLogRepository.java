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
 * 登录日志查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:40
 */
@Repository
public class LoginLogRepository extends EsBaseServiceImpl<LoginLogDO> implements EsBaseService<LoginLogDO> {

}
