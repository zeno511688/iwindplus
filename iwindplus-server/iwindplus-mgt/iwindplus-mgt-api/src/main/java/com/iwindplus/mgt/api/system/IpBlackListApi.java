/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.dto.IpBlackListSearchDTO;
import com.iwindplus.mgt.api.system.vo.IpBlackListPageVO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * IP黑名单相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface IpBlackListApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/ipBlackList/";

    /**
     * 查询所有.
     *
     * @return ResultVO<List < String>>
     */
    @Operation(summary = "查询所有")
    @GetMapping(API_PREFIX + "listIp")
    ResultVO<List<String>> listIp();

    /**
     * 分页查询.
     *
     * @param entity 查询条件
     * @return ResultVO<DbPageVO < IpBlackListPageVO>>
     */
    @Operation(summary = "分页查询")
    @GetMapping(API_PREFIX + "page")
    ResultVO<DbPageVO<IpBlackListPageVO>> page(@Validated IpBlackListSearchDTO entity);
}
