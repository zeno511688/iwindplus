/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.flow.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.flow.domain.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.domain.vo.FlowStartInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 流程实例相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24 15:12
 */
public interface FlowInstanceApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/flow/instance/";

    /**
     * 发起流程实例.
     *
     * @param entity 对象
     * @return ResultVO<FlowStartInstanceVO>
     */
    @Operation(summary = "发起流程实例")
    @PostMapping(API_PREFIX + "startInstance")
    ResultVO<FlowStartInstanceVO> startInstance(@RequestBody FlowStartInstanceDTO entity);
}
