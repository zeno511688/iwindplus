/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 角色相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24 15:12
 */
public interface RoleApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/role/";

    /**
     * 用户角色权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return ResultVO<List < RoleBaseVO>>
     */
    @GetMapping(API_PREFIX + "listCheckedByUserId")
    ResultVO<List<RoleBaseVO>> listCheckedByUserId(@RequestParam(value = "orgId") Long orgId, @RequestParam(value = "userId") Long userId);

}
