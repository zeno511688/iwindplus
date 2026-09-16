/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.upms;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.upms.RoleApi;
import com.iwindplus.mgt.api.upms.vo.RoleBaseVO;
import com.iwindplus.mgt.application.query.upms.permission.RoleQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class RoleApiImpl implements RoleApi {

    private final RoleQueryService roleQueryService;

    @Override
    public ResultVO<List<RoleBaseVO>> listCheckedByUserId(Long orgId, Long userId) {
        final List<RoleBaseVO> data = this.roleQueryService.listCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

}
