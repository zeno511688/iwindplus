/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.OrgVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 组织相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24 15:12
 */
public interface OrgApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/org/";

    /**
     * 查询用户组织.
     *
     * @param userId 用户主键
     * @return ResultVO<OrgBaseCheckedVO>
     */
    @Operation(summary = "查询用户组织")
    @GetMapping(API_PREFIX + "getOrgByUserId")
    ResultVO<OrgBaseCheckedVO> getOrgByUserId(@RequestParam(value = "userId") Long userId);

    /**
     * 查询用户组织主键.
     *
     * @param userId 用户主键
     * @return ResultVO<Long>
     */
    @Operation(summary = "查询用户组织主键")
    @GetMapping(API_PREFIX + "getOrgId")
    ResultVO<Long> getOrgId(@RequestParam(value = "userId") Long userId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < OrgVO>
     */
    @GetMapping(API_PREFIX + "getDetail")
    ResultVO<OrgVO> getDetail(@RequestParam(value = "id") Long id);
}
