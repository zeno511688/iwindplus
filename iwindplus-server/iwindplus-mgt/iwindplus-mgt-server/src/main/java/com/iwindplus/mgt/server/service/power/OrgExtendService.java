/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.OrgExtendDTO;

/**
 * 组织扩展业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface OrgExtendService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(OrgExtendDTO entity);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(OrgExtendDTO entity);
}
