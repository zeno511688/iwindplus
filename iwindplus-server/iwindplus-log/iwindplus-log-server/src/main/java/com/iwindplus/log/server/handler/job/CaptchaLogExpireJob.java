/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.handler.job;

import cn.hutool.core.date.DatePattern;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.log.server.service.MailCaptchaLogService;
import com.iwindplus.log.server.service.SmsCaptchaLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 验证码日志过期任务.
 *
 * @author zengdegui
 * @since 2024/11/19 01:42
 */
@Slf4j
@Component
@JobExecutor(name = "captchaLogExpireJob")
public class CaptchaLogExpireJob {

    @Resource
    private SmsCaptchaLogService smsCaptchaLogService;

    @Resource
    private MailCaptchaLogService mailCaptchaLogService;

    /**
     * 验证码日志过期清理.
     *
     * @param jobArgs    任务参数
     * @return ExecuteResult
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        long beginMillis = System.currentTimeMillis();

        log.info("验证码日志过期清理任务，参数={}，开始时间={}", jobArgs.getJobParams()
            , DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN));

        final boolean smsCaptchaLogResult = this.smsCaptchaLogService.removeExpireData();

        final boolean mailCaptchaLogResult = this.mailCaptchaLogService.removeExpireData();

        final long endTimeMillis = System.currentTimeMillis();
        log.info("验证码日志过期清理任务，短信执行结果={}，邮箱执行结果={}，结束时间={}，总执行毫秒数={}", smsCaptchaLogResult,
            mailCaptchaLogResult, DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);
        return ExecuteResult.success(smsCaptchaLogResult && mailCaptchaLogResult);
    }
}
