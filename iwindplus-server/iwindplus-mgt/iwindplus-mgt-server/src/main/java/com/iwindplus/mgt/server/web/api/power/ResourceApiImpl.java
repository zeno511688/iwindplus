/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.power.ResourceApi;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import com.iwindplus.mgt.server.service.power.ResourceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class ResourceApiImpl implements ResourceApi {

    private final ResourceService resourceService;

    @Override
    public ResultVO<Boolean> checkApiByUserId(Long orgId, Long userId, String requestMethod, String path) {
        final Boolean data = this.resourceService.checkApiByUserId(orgId, userId, requestMethod, path);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ResourceBaseExtendVO>> listApiCheckedByUserId(Long orgId, Long userId) {
        final List<ResourceBaseExtendVO> data = this.resourceService.listApiCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ResourceBaseVO>> listButtonCheckedByUserId(Long orgId, Long userId) {
        final List<ResourceBaseVO> data = this.resourceService.listButtonCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ResourceBaseExtendVO>> listAll() {
        final List<ResourceBaseExtendVO> data = this.resourceService.listAll();
        return ResultVO.success(data);
    }

}
