/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.upms;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.upms.ResourceApi;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseExtendVO;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseVO;
import com.iwindplus.mgt.application.query.upms.permission.ResourceQueryService;
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

    private final ResourceQueryService resourceQueryService;

    @Override
    public ResultVO<Boolean> checkApiByUserId(Long orgId, Long userId, String requestMethod, String path) {
        final Boolean data = this.resourceQueryService.checkApiByUserId(orgId, userId, requestMethod, path);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ResourceBaseExtendVO>> listApiCheckedByUserId(Long orgId, Long userId) {
        final List<ResourceBaseExtendVO> data = this.resourceQueryService.listApiCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ResourceBaseVO>> listButtonCheckedByUserId(Long orgId, Long userId) {
        final List<ResourceBaseVO> data = this.resourceQueryService.listButtonCheckedByUserId(orgId, userId);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ResourceBaseExtendVO>> listAll() {
        final List<ResourceBaseExtendVO> data = this.resourceQueryService.listAll();
        return ResultVO.success(data);
    }

}
