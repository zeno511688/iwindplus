/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.client;

import com.iwindplus.dtx.api.TccGlobalTxApi;
import com.iwindplus.dtx.domain.constant.DtxConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 全局事务客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = DtxConstant.DTX_SERVER_NAME,
    contextId = "tccGlobalTxClient"
)
public interface TccGlobalTxClient extends TccGlobalTxApi {

}
