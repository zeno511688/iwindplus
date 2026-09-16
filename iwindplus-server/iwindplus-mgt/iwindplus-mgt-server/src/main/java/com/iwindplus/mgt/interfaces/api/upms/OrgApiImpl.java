/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.upms;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.upms.OrgApi;
import com.iwindplus.mgt.api.upms.vo.OrgBaseCheckedVO;
import com.iwindplus.mgt.api.upms.vo.OrgVO;
import com.iwindplus.mgt.application.query.upms.organization.OrgQueryService;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class OrgApiImpl implements OrgApi {

    private final OrgApplicationService orgApplicationService;
    private final OrgQueryService orgQueryService;

    @Override
    public ResultVO<OrgBaseCheckedVO> getOrgByUserId(Long userId) {
        OrgBaseCheckedVO data = this.orgQueryService.getOrg(userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Long> getOrgId(Long userId) {
        final Long data = this.orgQueryService.getOrgId(userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<OrgVO> getDetail(Long id) {
        OrgVO data = this.orgQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
