/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.init;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.service.init.InitApplicationService;
import com.iwindplus.mgt.application.service.init.dto.InitUpmsDataDTO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("admin/mgt/init")
@Validated
@RequiredArgsConstructor
public class InitController extends BaseController {

    private final InitApplicationService initApplicationService;

    /**
     * 初始化数据（新组织）.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "初始化数据")
    @PostMapping("initData")
    public ResultVO<Boolean> initData(@RequestBody @Validated InitUpmsDataDTO entity) {
        Boolean data = this.initApplicationService.initData(entity);
        return ResultVO.success(data);
    }
}
