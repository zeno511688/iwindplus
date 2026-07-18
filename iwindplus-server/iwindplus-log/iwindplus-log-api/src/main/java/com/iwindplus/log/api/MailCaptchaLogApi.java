/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.api;

import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.domain.dto.MailCaptchaLogDTO;
import com.iwindplus.log.domain.dto.MailSendValidDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 邮箱验证码日志相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface MailCaptchaLogApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/mail/captcha/log/";

    /**
     * 添加邮箱验证码日志.
     *
     * @param entity 对象
     * @return ResultVO<String>
     */
    @Operation(summary = "添加邮箱验证码日志")
    @PostMapping(API_PREFIX + "save")
    ResultVO<String> save(@RequestBody @Validated({SaveGroup.class}) MailCaptchaLogDTO entity);

    /**
     * 校验是否可以发送.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "校验是否可以发送")
    @PostMapping("checkCanSend")
    ResultVO<Boolean> checkCanSend(@RequestBody @Validated MailSendValidDTO entity);

    /**
     * 校验验证码（邮箱）.
     *
     * @param tplCode 模板编码
     * @param mail    邮箱
     * @param captcha 验证码
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "校验验证码（邮箱）")
    @GetMapping("validate")
    ResultVO<Boolean> validate(
        @RequestParam(value = "tplCode") String tplCode,
        @RequestParam(value = "mail") String mail,
        @RequestParam(value = "captcha") String captcha);

    /**
     * 校验验证码（用户主键）.
     *
     * @param tplCode 模板配置编码
     * @param userId  用户主键
     * @param orgId   组织主键
     * @param captcha 验证码
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "校验验证码（用户主键）")
    @GetMapping("validateByUserId")
    ResultVO<Boolean> validateByUserId(
        @RequestParam("tplCode") String tplCode,
        @RequestParam("userId") Long userId,
        @RequestParam("orgId") Long orgId,
        @RequestParam("captcha") String captcha);


}
