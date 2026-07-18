/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.vo.AppApiVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.ServerApiApi;
import com.iwindplus.mgt.domain.dto.system.ServerApiDTO;
import com.iwindplus.mgt.domain.vo.system.ServerApiBaseVO;
import com.iwindplus.mgt.server.service.system.ServerApiService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务API相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class ServerApiApiImpl implements ServerApiApi {

    private final ServerApiService serverApiService;

    @Override
    public ResultVO<Boolean> saveOrEdit(AppApiVO entity) {
        boolean data = this.saveOrEditData(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<List<ServerApiBaseVO>> listApi() {
        final List<ServerApiBaseVO> data = this.serverApiService.listApi();
        return ResultVO.success(data);
    }

    private boolean saveOrEditData(AppApiVO entity) {
        if (Objects.isNull(entity) || CollUtil.isEmpty(entity.getApis())) {
            return false;
        }

        // 校验并收集
        List<ServerApiDTO> entityList = entity.getApis().stream()
            .filter(api -> {
                boolean ok = CharSequenceUtil.isAllNotBlank(
                    api.getControllerName(),
                    api.getRequestMethod(),
                    api.getApiName(),
                    api.getApiUrl());
                if (!ok) {
                    log.warn("API校验未通过={}", api);
                }
                return ok;
            })
            .map(api -> ServerApiDTO.builder()
                .appName(entity.getAppName())
                .controllerName(api.getControllerName())
                .requestMethod(api.getRequestMethod())
                .apiName(api.getApiName())
                .apiUrl(api.getApiUrl())
                .hideFlag(api.getHideFlag())
                .build())
            .collect(Collectors.toList());

        if (entityList.isEmpty()) {
            log.warn("经过校验后，API列表为空，无法保存");
            return false;
        }

        return this.serverApiService.saveOrEditBatch(entityList);
    }
}
