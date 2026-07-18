/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.domain.dto.MailCaptchaLogDTO;
import com.iwindplus.log.domain.dto.MailCaptchaLogSearchDTO;
import com.iwindplus.log.domain.vo.MailCaptchaLogPageVO;
import com.iwindplus.log.domain.vo.MailCaptchaLogVO;
import com.iwindplus.log.server.service.MailCaptchaLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证码日志相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "邮箱验证码日志接口")
@Slf4j
@RestController
@RequestMapping("admin/log/mail/captcha/log")
@Validated
@RequiredArgsConstructor
public class MailCaptchaLogController extends BaseController {

    private final MailCaptchaLogService mailCaptchaLogService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    public ResultVO<String> save(@RequestBody @Validated({SaveGroup.class}) MailCaptchaLogDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        entity.setUserId(this.getUserInfo().getUserId());
        String data = this.mailCaptchaLogService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    public ResultVO<Boolean> removeByIds(@RequestParam List<String> ids) {
        boolean data = this.mailCaptchaLogService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 清理过期的数据.
     *
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "清理过期的数据")
    @DeleteMapping("removeExpireData")
    public ResultVO<Boolean> remove() {
        boolean data = this.mailCaptchaLogService.removeExpireData();
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < MailCaptchaLogPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<MailCaptchaLogPageVO>> page(@Validated MailCaptchaLogSearchDTO entity) {
        IPage<MailCaptchaLogPageVO> data = this.mailCaptchaLogService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<MailCaptchaLogVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<MailCaptchaLogVO> getDetail(@RequestParam String id) {
        MailCaptchaLogVO data = this.mailCaptchaLogService.getDetail(id);
        return ResultVO.success(data);
    }

}
