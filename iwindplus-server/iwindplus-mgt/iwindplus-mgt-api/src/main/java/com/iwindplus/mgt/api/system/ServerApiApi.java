/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.vo.AppApiVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiBaseVO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 服务API相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface ServerApiApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/serverApi/";

    /**
     * 添加或编辑.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加或编辑")
    @PostMapping(API_PREFIX + "saveOrEdit")
    ResultVO<Boolean> saveOrEdit(@RequestBody AppApiVO entity);

    /**
     * 获取所有服务API.
     *
     * @return ResultVO<List < ServerApiBaseVO>>
     */
    @GetMapping(API_PREFIX + "listApi")
    ResultVO<List<ServerApiBaseVO>> listApi();
}
