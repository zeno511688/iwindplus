/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.client.power;

import com.iwindplus.mgt.api.power.OrgApi;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 组织客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = MgtConstant.MGT_SERVER_NAME,
    contextId = "orgClient"
)
public interface OrgClient extends OrgApi {

}
