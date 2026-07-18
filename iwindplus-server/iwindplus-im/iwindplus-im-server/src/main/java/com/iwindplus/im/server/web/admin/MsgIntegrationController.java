/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.web.admin;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.domain.dto.MsgIntegrationDetailDTO;
import com.iwindplus.im.domain.vo.MsgIntegrationVO;
import com.iwindplus.im.server.service.MsgIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
public class MsgIntegrationController extends BaseController {
    @Resource
    private MsgIntegrationService msgIntegrationService;

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
        MsgIntegrationVO data = this.msgIntegrationService.getMsg(entity);
        return ResultVO.success(data);
    }
}
