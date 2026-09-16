/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.system;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.ClientApi;
import com.iwindplus.mgt.api.system.dto.ClientDTO;
import com.iwindplus.mgt.api.system.vo.ClientBaseVO;
import com.iwindplus.mgt.api.system.vo.ClientVO;
import com.iwindplus.mgt.application.query.system.app.ClientQueryService;
import com.iwindplus.mgt.application.service.system.app.ClientApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class ClientApiImpl implements ClientApi {

    private final ClientApplicationService clientApplicationService;
    private final ClientQueryService clientQueryService;

    @Override
    public ResultVO<ClientBaseVO> save(ClientDTO entity) {
        ClientBaseVO data = this.clientApplicationService.save(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<ClientVO> getDetail(String id) {
        ClientVO data = this.clientQueryService.getDetail(id);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<ClientVO> getByClientId(String clientId) {
        ClientVO data = this.clientQueryService.getByClientId(clientId);
        return ResultVO.success(data);
    }
}
