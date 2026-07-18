/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.dtx.tcc.domain.annotation.TccBranchTx;
import com.iwindplus.mgt.domain.dto.InitDataDTO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 初始化相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "初始化接口")
@Slf4j
@RestController
@Hidden
@RequestMapping("admin/setup/init")
@Validated
public class InitController extends BaseController {

    @Resource
    private HttpClientExecutorStrategyFactory factory;

    @PostMapping("tryB")
    @TccBranchTx(confirmUrl = "/admin/setup/init/confirmB", cancelUrl = "/admin/setup/init/cancelB1")
    public ResultVO<Boolean> tryB1(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
    }

    @PostMapping("confirmB")
    public ResultVO<Boolean> confirmB(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
    }

    @PostMapping("cancelB")
    public ResultVO<Boolean> cancelB(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
    }
}
