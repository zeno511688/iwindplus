/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.power.OrgApi;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.OrgVO;
import com.iwindplus.mgt.server.service.power.OrgService;
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

    private final OrgService orgService;

    @Override
    public ResultVO<OrgBaseCheckedVO> getOrgByUserId(Long userId) {
        OrgBaseCheckedVO data = this.orgService.getOrg(userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Long> getOrgId(Long userId) {
        final Long data = this.orgService.getOrgId(userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<OrgVO> getDetail(Long id) {
        OrgVO data = this.orgService.getDetail(id);
        return ResultVO.success(data);
    }
}
