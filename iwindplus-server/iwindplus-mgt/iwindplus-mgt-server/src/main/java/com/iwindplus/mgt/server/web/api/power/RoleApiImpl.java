/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.power.RoleApi;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.server.service.power.RoleService;
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

    private final RoleService roleService;

    @Override
    public ResultVO<List<RoleBaseVO>> listCheckedByUserId(Long orgId, Long userId) {
        final List<RoleBaseVO> data = this.roleService.listCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

}
