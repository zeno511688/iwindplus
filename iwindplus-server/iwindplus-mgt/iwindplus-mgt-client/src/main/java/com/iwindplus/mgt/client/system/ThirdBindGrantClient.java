/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.client.system;

import com.iwindplus.mgt.api.system.ThirdBindGrantApi;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 第三方绑定授权客户端.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
@FeignClient(
    value = MgtConstant.MGT_SERVER_NAME,
    contextId = "thirdBindGrantClient"
)
public interface ThirdBindGrantClient extends ThirdBindGrantApi {

}
