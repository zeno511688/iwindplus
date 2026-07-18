/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.service;

import com.iwindplus.base.domain.dto.UserExtendFunctionValidDTO;
import com.iwindplus.base.domain.vo.UserExtendFunctionValidVO;

/**
 * 操作业务层接口.
 *
 * @author zengdegui
 * @since 2024/4/28
 */
public interface OperateService {

    /**
     * 校验扩展功能是否正确.
     *
     * @param entity 对象
     * @return UserExtendFunctionValidVO
     */
    UserExtendFunctionValidVO checkExtendFunctionByUserId(UserExtendFunctionValidDTO entity);
}
