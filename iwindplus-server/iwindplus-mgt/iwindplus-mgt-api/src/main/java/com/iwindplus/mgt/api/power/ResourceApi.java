/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.api.power;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 资源相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24 15:12
 */
public interface ResourceApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/resource/";

    /**
     * 校验用户API权限.
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param requestMethod 请求方式
     * @param path          路径
     * @return ResultVO<Boolean>
     */
    @GetMapping(API_PREFIX + "checkApiByUserId")
    ResultVO<Boolean> checkApiByUserId(
        @RequestParam(value = "orgId") Long orgId,
        @RequestParam(value = "userId") Long userId,
        @RequestParam(value = "requestMethod") String requestMethod,
        @RequestParam(value = "path") String path);

    /**
     * 用户API权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return ResultVO<List < ResourceBaseExtendVO>>
     */
    @GetMapping(API_PREFIX + "listApiCheckedByUserId")
    ResultVO<List<ResourceBaseExtendVO>> listApiCheckedByUserId(@RequestParam(value = "orgId") Long orgId,
        @RequestParam(value = "userId") Long userId);

    /**
     * 用户按钮权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return ResultVO<List < ResourceBaseVO>>
     */
    @GetMapping(API_PREFIX + "listButtonCheckedByUserId")
    ResultVO<List<ResourceBaseVO>> listButtonCheckedByUserId(@RequestParam(value = "orgId") Long orgId,
        @RequestParam(value = "userId") Long userId);

    /**
     * 获取所有资源.
     *
     * @return ResultVO<List < ResourceBaseExtendVO>>
     */
    @GetMapping(API_PREFIX + "listAll")
    ResultVO<List<ResourceBaseExtendVO>> listAll();
}
