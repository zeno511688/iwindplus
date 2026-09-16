/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.interfaces.controller;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.application.query.MsgIntegrationQueryService;
import com.iwindplus.im.application.query.dto.MsgIntegrationDetailDTO;
import com.iwindplus.im.application.query.vo.MsgIntegrationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息集成相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2023/12/04 23:10
 */
@Tag(name = "消息集成接口")
@Slf4j
@RestController
@RequestMapping("admin/im/msg/integration")
@Validated
@RequiredArgsConstructor
public class MsgIntegrationController extends BaseController {

    private final MsgIntegrationQueryService msgIntegrationQueryService;

    /**
     * 获取消息.
     *
     * @param entity 对象
     * @return ResultVO<MsgIntegrationVO>
     */
    @Operation(summary = "获取消息")
    @GetMapping("getMsg")
    public ResultVO<MsgIntegrationVO> getMsg(MsgIntegrationDetailDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        MsgIntegrationVO data = this.msgIntegrationQueryService.getMsg(entity);
        return ResultVO.success(data);
    }
}
