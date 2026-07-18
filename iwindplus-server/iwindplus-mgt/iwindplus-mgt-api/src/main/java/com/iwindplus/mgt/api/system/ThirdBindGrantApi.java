/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSaveEditDTO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 第三方绑定授权相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface ThirdBindGrantApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/thirdBindGrant/";

    /**
     * 添加或编辑（绑定openid）.
     *
     * @param entity 对象
     * @return ResultVO<ThirdBindGrantResultVO>
     */
    @Operation(summary = "查询所有")
    @PostMapping(API_PREFIX + "saveOrEdit")
    ResultVO<ThirdBindGrantResultVO> saveOrEdit(@RequestBody @Validated({SaveGroup.class}) ThirdBindGrantSaveEditDTO entity);
}
