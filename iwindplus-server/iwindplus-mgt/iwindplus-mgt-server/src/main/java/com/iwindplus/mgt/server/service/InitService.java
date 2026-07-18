/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service;

import com.iwindplus.mgt.domain.dto.InitDataDTO;

/**
 * 初始化业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface InitService {

    /**
     * 初始化数据.
     *
     * @param entity 对象
     * @return Boolean
     */
    Boolean initData(InitDataDTO entity);
}
