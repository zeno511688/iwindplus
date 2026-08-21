/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.dto.address;

import java.util.List;
import lombok.Data;

/**
 * IP138 地址查询响应.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
public class Ip138AddressDTO {

    /**
     * 地址数据，通常第2项为省份，第3项为城市.
     */
    private List<String> data;
}
