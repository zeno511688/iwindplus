/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.api;

import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.domain.dto.MailLogDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 邮箱日志相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface MailLogApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/mail/log/";

    /**
     * 添加邮箱日志.
     *
     * @param entity 对象
     * @return ResultVO<String>
     */
    @Operation(summary = "添加邮箱日志")
    @PostMapping(API_PREFIX + "save")
    ResultVO<String> save(@RequestBody @Validated({SaveGroup.class}) MailLogDTO entity);
}
