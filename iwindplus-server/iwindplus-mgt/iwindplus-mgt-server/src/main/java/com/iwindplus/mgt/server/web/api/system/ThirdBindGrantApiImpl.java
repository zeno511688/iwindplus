/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.system;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.ThirdBindGrantApi;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSaveEditDTO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import com.iwindplus.mgt.server.service.system.ThirdBindGrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三方绑定授权相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class ThirdBindGrantApiImpl implements ThirdBindGrantApi {

    private final ThirdBindGrantService thirdBindGrantService;

    @Override
    public ResultVO<ThirdBindGrantResultVO> saveOrEdit(ThirdBindGrantSaveEditDTO entity) {
        ThirdBindGrantResultVO data = this.thirdBindGrantService.saveOrEdit(entity);
        return ResultVO.success(data);
    }
}
