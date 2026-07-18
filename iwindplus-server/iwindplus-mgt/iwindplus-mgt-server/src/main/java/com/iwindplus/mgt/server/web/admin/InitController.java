/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.dtx.tcc.domain.annotation.TccBranchTx;
import com.iwindplus.dtx.tcc.domain.annotation.TccGlobalTx;
import com.iwindplus.flow.client.FlowInstanceClient;
import com.iwindplus.flow.domain.dto.FlowInstanceCallbackExtDTO;
import com.iwindplus.flow.domain.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.domain.vo.FlowStartInstanceVO;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import com.iwindplus.mgt.domain.dto.InitDataDTO;
import com.iwindplus.mgt.server.service.InitService;
import com.iwindplus.setup.domain.constant.SetupConstant;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("admin/mgt/init")
@Validated
public class InitController extends BaseController {

    @Resource
    private InitService initService;

    @Resource
    private HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;

    @Resource
    private FlowInstanceClient flowInstanceClient;

    /**
     * 初始化数据（新组织）.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "初始化数据")
    @PostMapping("initData")
    public ResultVO<Boolean> initData(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = this.initService.initData(entity);
        return ResultVO.success(data);
    }

    @PostMapping("createOrder")
    @TccGlobalTx(bizType = "createOrder")
    public ResultVO<Boolean> createOrder(@RequestBody @Validated InitDataDTO entity) {
        final String urlPrefix = String.format("%s%s", NetWorkConstant.LB_PREFIX, MgtConstant.MGT_SERVER_NAME);
        final ResultVO<Boolean> result =
            this.httpClientExecutorStrategyFactory.getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)
                .post(
                    urlPrefix + "/admin/mgt/init/tryA",
                    entity,
                    null,
                    new TypeReference<>() {
                    }
                );
        result.errorThrow();

        final String urlPrefix2 = String.format("%s%s", NetWorkConstant.LB_PREFIX, SetupConstant.SETUP_SERVER_NAME);
        final ResultVO<Boolean> result2 =
            this.httpClientExecutorStrategyFactory.getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)
                .post(
                    urlPrefix2 + "/admin/setup/init/tryB",
                    entity,
                    null,
                    new TypeReference<>() {
                    }
                );
        result2.errorThrow();
        Boolean data = true;
        return ResultVO.success(data);
    }

    @PostMapping("tryA")
    @TccBranchTx(confirmUrl = "/admin/mgt/init/confirmA", cancelUrl = "/admin/mgt/init/cancelA")
    public ResultVO<Boolean> tryA(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
    }

    @PostMapping("confirmA")
    public ResultVO<Boolean> confirmA(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
        //throw new RuntimeException("RuntimeException");
    }

    @PostMapping("cancelA")
    public ResultVO<Boolean> cancelA(@RequestBody @Validated InitDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
    }

    @PostMapping("startFlow")
    public ResultVO<Boolean> startFlow(@RequestBody @Validated FlowStartInstanceDTO entity) {
        entity.setCurrentUser(this.getUserInfo());
        final ResultVO<FlowStartInstanceVO> flowStartInstanceResultVO = flowInstanceClient.startInstance(entity);
        flowStartInstanceResultVO.errorThrow();
        return ResultVO.success(flowStartInstanceResultVO.bizSuccess());
    }

    @PostMapping("flowCallback")
    public ResultVO<Boolean> flowCallback(@RequestBody @Validated FlowInstanceCallbackExtDTO entity) {
        System.out.println(JacksonUtil.toJsonStr(entity));
        return ResultVO.success(true);
    }
}
