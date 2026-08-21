/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.example;

import com.iwindplus.base.http.client.integration.annotation.SumSubWebhookListener;
import com.iwindplus.base.http.client.integration.domain.constant.SumSubConstant;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubWebhookDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SumSub Webhook监听器注解使用示例.
 * <p>
 * 业务方可以参考此示例，使用@SumSubWebhookListener注解在任意方法上处理Webhook事件。
 * 这种方式比实现接口更灵活，无需创建专门的处理器类。
 * </p>
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
@Component
public class SumSubWebhookListenerExample {

    /**
     * 使用注解方式处理申请人审核完成事件（使用常量）.
     */
    @SumSubWebhookListener(SumSubConstant.WEBHOOK_TYPE_APPLICANT_REVIEWED)
    public void handleApplicantReviewed(SumSubWebhookDTO webhookData) {
        log.info("Applicant reviewed: applicantId={}, reviewResult={}",
            webhookData.getApplicantId(),
            webhookData.getReviewResult() != null ? webhookData.getReviewResult().getReviewResult() : null);

        // TODO: 实现具体的业务逻辑，例如：
        // 1. 更新数据库中的KYC状态
        // 2. 发送通知给用户
        // 3. 触发后续业务流程
    }

    /**
     * 使用注解方式处理申请人待审核事件（使用字符串）.
     */
    @SumSubWebhookListener("applicantPending")
    public void handleApplicantPending(SumSubWebhookDTO webhookData) {
        log.info("Applicant pending: applicantId={}", webhookData.getApplicantId());
        // TODO: 实现具体的业务逻辑
    }

    /**
     * 使用注解方式处理文档上传事件（使用常量）.
     */
    @SumSubWebhookListener(SumSubConstant.WEBHOOK_TYPE_APPLICANT_DOCUMENT_UPLOADED)
    public void handleDocumentUploaded(SumSubWebhookDTO webhookData) {
        log.info("Document uploaded: applicantId={}, documentId={}",
            webhookData.getApplicantId(), webhookData.getDocumentId());
        // TODO: 实现具体的业务逻辑
    }

    /**
     * 处理自定义事件类型（演示扩展性）.
     * <p>
     * 业务方可以自定义事件类型，无需修改底层代码
     * </p>
     */
    @SumSubWebhookListener("customEventType")
    public void handleCustomEvent(SumSubWebhookDTO webhookData) {
        log.info("Custom event: type={}, applicantId={}",
            webhookData.getType(), webhookData.getApplicantId());
        // TODO: 实现具体的业务逻辑
    }
}
