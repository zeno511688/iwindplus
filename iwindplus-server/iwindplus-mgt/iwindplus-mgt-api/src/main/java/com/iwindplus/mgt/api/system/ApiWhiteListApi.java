/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.dto.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.api.system.vo.ApiWhiteListPageVO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * API白名单相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface ApiWhiteListApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/apiWhiteList/";

    /**
     * 查询所有.
     *
     * @return ResultVO<List < String>>
     */
    @Operation(summary = "查询所有")
    @GetMapping(API_PREFIX + "listApi")
    ResultVO<List<String>> listApi();

    /**
     * 分页查询.
     *
     * @param entity 查询条件
     * @return ResultVO<DbPageVO < ApiWhiteListPageVO>>
     */
    @Operation(summary = "分页查询")
    @GetMapping(API_PREFIX + "page")
    ResultVO<DbPageVO<ApiWhiteListPageVO>> page(@Validated ApiWhiteListSearchDTO entity);
}
