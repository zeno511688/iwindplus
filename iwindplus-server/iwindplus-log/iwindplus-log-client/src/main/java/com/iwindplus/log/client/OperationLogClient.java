/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.client;

import com.iwindplus.log.api.OperationLogApi;
import com.iwindplus.log.domain.constant.LogConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 操作日志客户端.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@FeignClient(
    value = LogConstant.LOG_SERVER_NAME,
    contextId = "operationLogClient"
)
public interface OperationLogClient extends OperationLogApi {

}
