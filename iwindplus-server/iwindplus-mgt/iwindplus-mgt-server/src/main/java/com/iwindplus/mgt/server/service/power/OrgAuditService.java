/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.OrgAuditDTO;
import com.iwindplus.mgt.domain.vo.power.OrgAuditVO;
import java.util.List;

/**
 * 组织审核业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface OrgAuditService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(OrgAuditDTO entity);

    /**
     * 通过组织主键查询.
     *
     * @param orgId 组织主键
     * @return List<OrgAuditVO>
     */
    List<OrgAuditVO> listByOrgId(Long orgId);
}
