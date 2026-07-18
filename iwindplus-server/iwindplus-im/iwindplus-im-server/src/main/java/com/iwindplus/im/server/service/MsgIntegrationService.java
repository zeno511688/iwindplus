/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.iwindplus.im.domain.dto.MsgIntegrationDetailDTO;
import com.iwindplus.im.domain.vo.MsgIntegrationVO;

/**
 * 消息集成业务层接口类.
 *
 * @author zengdegui
 * @since 2023/12/04 23:22
 */
public interface MsgIntegrationService {
    /**
     * 获取消息.
     *
     * @param entity 对象
     * @return MsgIntegrationVO
     */
    MsgIntegrationVO getMsg(MsgIntegrationDetailDTO entity);
}
