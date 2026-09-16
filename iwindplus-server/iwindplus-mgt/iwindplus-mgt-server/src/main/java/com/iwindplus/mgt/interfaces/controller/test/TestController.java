/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.dtx.tcc.domain.annotation.TccBranchTx;
import com.iwindplus.dtx.tcc.domain.annotation.TccGlobalTx;
import com.iwindplus.flow.api.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.api.vo.FlowStartInstanceVO;
import com.iwindplus.flow.client.FlowInstanceClient;
import com.iwindplus.integr.common.constant.IntegrConstant;
import com.iwindplus.mgt.application.service.init.InitApplicationService;
import com.iwindplus.mgt.application.service.init.dto.InitUpmsDataDTO;
import com.iwindplus.mgt.common.constant.MgtConstant;
import feign.Client;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "测试接口")
@Slf4j
@RestController
@Hidden
@RequestMapping("admin/mgt/test")
@Validated
public class TestController extends BaseController {

    @Resource
    private InitApplicationService initApplicationService;

    @Resource
    private HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

    @Resource
    private FlowInstanceClient flowInstanceClient;

    @Resource
    private ApplicationContext applicationContext;

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

    @PostMapping("createOrder")
    @TccGlobalTx(bizType = "createOrder")
    public ResultVO<Boolean> createOrder(@RequestBody @Validated InitUpmsDataDTO entity) {
        final String urlPrefix = String.format("%s%s", NetWorkConstant.LB_PREFIX, MgtConstant.MGT_SERVER_NAME);
        final ResultVO<Boolean> result =
            this.httpClientExecuteHandlerFactory.getHandler(HttpClientTypeEnum.REST_CLIENT)
                .post(
                    urlPrefix + "/admin/mgt/init/tryA",
                    entity,
                    null,
                    new TypeReference<>() {
                    }
                );
        result.errorThrow();

        final String urlPrefix2 = String.format("%s%s", NetWorkConstant.LB_PREFIX, IntegrConstant.INTEGR_SERVER_NAME);
        final ResultVO<Boolean> result2 =
            this.httpClientExecuteHandlerFactory.getHandler(HttpClientTypeEnum.REST_CLIENT)
                .post(
                    urlPrefix2 + "/admin/integr/init/tryB",
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
    public ResultVO<Boolean> tryA(@RequestBody @Validated InitUpmsDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
    }

    @PostMapping("confirmA")
    public ResultVO<Boolean> confirmA(@RequestBody @Validated InitUpmsDataDTO entity) {
        Boolean data = true;
        return ResultVO.success(data);
        //throw new RuntimeException("RuntimeException");
    }

    @PostMapping("cancelA")
    public ResultVO<Boolean> cancelA(@RequestBody @Validated InitUpmsDataDTO entity) {
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

    @GetMapping("checkFeignHttpClient")
    public String check() {
        // 1. 检查自定义的 CloseableHttpClient Bean 是否存在
        CloseableHttpClient httpClient = applicationContext.getBean(CloseableHttpClient.class);
        System.out.println("自定义HttpClient实例：" + httpClient);

        // 2. 检查 Feign 最终使用的 Client 类型
        Client feignClient = applicationContext.getBean(Client.class);
        System.out.println("Feign底层客户端类型：" + feignClient.getClass().getName());

        return "校验完成";
    }
}
