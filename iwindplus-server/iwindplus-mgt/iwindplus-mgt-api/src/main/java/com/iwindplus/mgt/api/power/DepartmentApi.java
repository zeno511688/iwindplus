/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.vo.power.DepartmentBaseVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 部门相关接口.
 *
 * @author zengdegui
 * @since 2026/05/22 19:16
 */
public interface DepartmentApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/department/";

    /**
     * 用户所属部门.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return ResultVO<List < DepartmentBaseVO>>
     */
    @GetMapping(API_PREFIX + "listCheckedByUserId")
    ResultVO<List<DepartmentBaseVO>> listCheckedByUserId(@RequestParam(value = "orgId") Long orgId, @RequestParam(value = "userId") Long userId);
}
