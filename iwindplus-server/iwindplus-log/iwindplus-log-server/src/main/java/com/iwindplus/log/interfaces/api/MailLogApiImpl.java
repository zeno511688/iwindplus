/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.interfaces.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.MailLogApi;
import com.iwindplus.log.api.dto.MailLogDTO;
import com.iwindplus.log.application.service.MailLogApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ws消息推送相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class MailLogApiImpl extends BaseController implements MailLogApi {

    private final MailLogApplicationService mailLogApplicationService;

    @Override
    public ResultVO<String> save(MailLogDTO entity) {
        final String data = this.mailLogApplicationService.save(entity);
        return ResultVO.success(data);
    }
}
