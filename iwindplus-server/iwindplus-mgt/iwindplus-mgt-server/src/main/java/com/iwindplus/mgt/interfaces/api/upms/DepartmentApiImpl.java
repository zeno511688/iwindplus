/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.upms;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.upms.DepartmentApi;
import com.iwindplus.mgt.api.upms.vo.DepartmentBaseVO;
import com.iwindplus.mgt.application.query.upms.organization.DepartmentQueryService;
import com.iwindplus.mgt.application.service.upms.organization.DepartmentApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class DepartmentApiImpl implements DepartmentApi {

    private final DepartmentApplicationService departmentApplicationService;
    private final DepartmentQueryService departmentQueryService;

    @Override
    public ResultVO<List<DepartmentBaseVO>> listCheckedByUserId(Long orgId, Long userId) {
        final List<DepartmentBaseVO> data = this.departmentQueryService.listCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }
}
