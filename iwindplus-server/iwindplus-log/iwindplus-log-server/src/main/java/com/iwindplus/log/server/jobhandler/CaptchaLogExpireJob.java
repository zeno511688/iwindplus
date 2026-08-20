/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.jobhandler;

import cn.hutool.core.date.DatePattern;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.log.server.service.MailCaptchaLogService;
import com.iwindplus.log.server.service.SmsCaptchaLogService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
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
public class CaptchaLogExpireJob {

    @Resource
    private SmsCaptchaLogService smsCaptchaLogService;

    @Resource
    private MailCaptchaLogService mailCaptchaLogService;

    /**
     * 验证码日志过期清理.
     */
    @XxlJob("captchaLogExpireJob")
    public void jobExecute() {
        final long beginMillis = System.currentTimeMillis();
        final String start = DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN);

        final int shardIndex = Math.max(XxlJobHelper.getShardIndex(), 0);
        final int shardTotal = Math.max(XxlJobHelper.getShardTotal(), 1);
        final String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("验证码日志过期清理任务，参数={}，开始时间={}，分片索引={}, 分片总数={}", jobParam, start, shardIndex, shardTotal);

        final boolean smsCaptchaLogResult = this.smsCaptchaLogService.removeExpireData();
        final boolean mailCaptchaLogResult = this.mailCaptchaLogService.removeExpireData();

        final long endTimeMillis = System.currentTimeMillis();
        XxlJobHelper.log("验证码日志过期清理任务，短信执行结果={}，邮箱执行结果={}，结束时间={}，总执行毫秒数={}", smsCaptchaLogResult,
            mailCaptchaLogResult, DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);

        XxlJobHelper.handleSuccess();
    }
}
