/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.dto.UploadByteDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.vo.MailVO;
import com.iwindplus.base.mail.service.MailService;
import com.iwindplus.base.util.CryptoUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

/**
 * 邮箱业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class MailServiceImpl extends AbstractMailBaseServiceImpl implements MailService {

    @Override
    public Mono<MailVO> send(MailDTO entity) {
        if (entity == null
            || CharSequenceUtil.isBlank(entity.getSubject())
            || CharSequenceUtil.isBlank(entity.getContent())
            || CollUtil.isEmpty(entity.getTos())) {
            return Mono.just(MailVO.builder()
                .result(false)
                .errorMsg(BizCodeEnum.PARAM_ERROR.getBizCode())
                .build());
        }

        String bizNumber = bizNumber(entity);

        Mono<MailVO> mono = getConfig().getEnableRetry()
            ? sendWithRetry(entity, bizNumber)
            : sendOnce(entity, bizNumber);

        return mono.subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<MailVO> sendOnce(MailDTO dto, String bizNumber) {
        return doSend(dto)
            .then(Mono.just(MailVO.ok(bizNumber, 1)))
            .onErrorResume(ex -> {
                final Throwable cause = ExceptionUtils.getRootCause(ex);
                String msg = "邮件发送失败：" + (cause != null ? cause : ex).getMessage();
                log.error(msg, ex);

                return Mono.just(MailVO.fail(bizNumber, 1, msg));
            });
    }

    private Mono<MailVO> sendWithRetry(MailDTO dto, String bizNumber) {
        AtomicInteger retried = new AtomicInteger(1);
        return doSend(dto)
            .retryWhen(Retry.backoff(getConfig().getMaxAttempts(), getConfig().getPeriod())
                .maxBackoff(getConfig().getMaxPeriod())
                .jitter(0.5)
                .doBeforeRetry(signal -> {
                    retried.getAndIncrement();

                    log.warn("邮件发送失败（第{}次重试）", signal.totalRetries() + 1);
                }))
            .then(Mono.just(MailVO.ok(bizNumber, 1)))
            .onErrorResume(ex -> {
                final Throwable cause = ExceptionUtils.getRootCause(ex);
                String msg = "邮件发送失败，已重试 " + (retried.get() - 1) + " 次：" + (cause != null ? cause : ex).getMessage();
                log.error(msg, ex);

                return Mono.just(MailVO.fail(bizNumber, retried.get(), msg));
            });

    }

    private Mono<Void> doSend(MailDTO dto) {
        return Mono.<Void>fromRunnable(() -> {
                try {
                    sendMailMessage(dto);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    private void sendMailMessage(MailDTO entity) throws Exception {
        JavaMailSenderImpl sender = buildSender();
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, this.getConfig().getDefaultEncoding().name());

        // 设置收件人
        helper.setTo(entity.getTos().toArray(String[]::new));
        if (CollUtil.isNotEmpty(entity.getCcs())) {
            helper.setCc(entity.getCcs().toArray(String[]::new));
        }
        if (CollUtil.isNotEmpty(entity.getBccs())) {
            helper.setBcc(entity.getBccs().toArray(String[]::new));
        }

        // 设置发件人
        InternetAddress from = new InternetAddress(sender.getUsername());
        if (CharSequenceUtil.isNotBlank(this.getConfig().getNickName())) {
            from.setPersonal(this.getConfig().getNickName(), sender.getDefaultEncoding());
        }
        helper.setFrom(from);

        // 设置主题和内容
        helper.setSubject(entity.getSubject());
        helper.setText(entity.getContent(), Optional.ofNullable(entity.getHtml()).orElse(false));

        // 添加附件
        this.addAttachments(helper, entity.getAttachments());

        // 发送
        sender.send(message);
    }

    private JavaMailSenderImpl buildSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(CharSequenceUtil.trimToNull(getConfig().getHost()));
        Optional.ofNullable(getConfig().getPort()).ifPresent(mailSender::setPort);
        mailSender.setUsername(CharSequenceUtil.trimToNull(getConfig().getUsername()));
        mailSender.setPassword(CharSequenceUtil.trimToNull(getConfig().getPassword()));
        mailSender.setProtocol(CharSequenceUtil.trimToNull(getConfig().getProtocol()));
        mailSender.setDefaultEncoding(this.getConfig().getDefaultEncoding().name());

        if (MapUtil.isNotEmpty(getConfig().getProperties())) {
            Properties p = new Properties();
            getConfig().getProperties().forEach(p::put);
            mailSender.setJavaMailProperties(p);
        }
        return mailSender;
    }

    private String bizNumber(MailDTO entity) {
        String param = new StringBuilder()
            .append(entity.getSubject())
            .append(entity.getContent())
            .append(String.join(",", entity.getTos()))
            .toString();
        return CryptoUtil.encryptBySm3(param);
    }

    private void addAttachments(MimeMessageHelper helper, List<UploadByteDTO> attachments) throws MessagingException {
        if (CollUtil.isEmpty(attachments)) {
            return;
        }

        for (UploadByteDTO attachment : attachments) {
            String filename = attachment.getSourceFileName();
            Byte[] data = attachment.getData();
            if (data == null || data.length == 0) {
                log.warn("附件 [{}] 数据为空，跳过添加", filename);
                continue;
            }

            String contentType = CharSequenceUtil.isNotBlank(attachment.getContentType())
                ? attachment.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            helper.addAttachment(filename, new ByteArrayResource(ArrayUtil.unWrap(data)), contentType);
        }
    }

}
