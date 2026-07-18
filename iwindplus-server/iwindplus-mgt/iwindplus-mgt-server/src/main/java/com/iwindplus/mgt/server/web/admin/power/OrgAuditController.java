/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.vo.power.OrgAuditVO;
import com.iwindplus.mgt.server.service.power.OrgAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织审核相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "组织审核接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/org/audit")
@Validated
@RequiredArgsConstructor
public class OrgAuditController extends BaseController {

    private final OrgAuditService orgAuditService;

    /**
     * 组织审核信息.
     *
     * @param orgId 组织主键
     * @return ResultVO < List < OrgAuditVO>>
     */
    @Operation(summary = "组织审核信息")
    @GetMapping("listByOrgId")
    public ResultVO<List<OrgAuditVO>> listByOrgId(@RequestParam(required = false) Long orgId) {
        orgId = Optional.ofNullable(orgId).orElse(this.getUserInfo().getOrgId());
        List<OrgAuditVO> data = this.orgAuditService.listByOrgId(orgId);
        return ResultVO.success(data);
    }

}
