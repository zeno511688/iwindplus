/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.dto.system.ClientDTO;
import com.iwindplus.mgt.domain.vo.system.ClientBaseVO;
import com.iwindplus.mgt.domain.vo.system.ClientVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 应用相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface ClientApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/client/";

    /**
     * 添加客户端.
     *
     * @param entity 对象
     * @return ResultVO<ClientBaseVO>
     */
    @Operation(summary = "添加客户端")
    @PostMapping(API_PREFIX + "save")
    ResultVO<ClientBaseVO> save(@RequestBody @Validated({SaveGroup.class}) ClientDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < ClientVO>
     */
    @Operation(summary = "详情")
    @GetMapping(API_PREFIX + "getDetail")
    ResultVO<ClientVO> getDetail(@RequestParam(value = "id") String id);

    /**
     * 通过客户端id查询.
     *
     * @param clientId 客户端id
     * @return ResultVO<ClientVO>
     */
    @Operation(summary = "通过客户端id查询")
    @GetMapping(API_PREFIX + "getByClientId")
    ResultVO<ClientVO> getByClientId(@RequestParam(value = "clientId") String clientId);
}
