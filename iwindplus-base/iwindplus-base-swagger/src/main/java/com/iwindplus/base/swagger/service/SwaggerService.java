/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.swagger.service;

import com.iwindplus.base.domain.vo.AppApiVO;

/**
 * swagger业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface SwaggerService {

    /**
     * 获取应用API信息.
     *
     * @return AppApiVO
     */
    AppApiVO getServerInfo();
}
