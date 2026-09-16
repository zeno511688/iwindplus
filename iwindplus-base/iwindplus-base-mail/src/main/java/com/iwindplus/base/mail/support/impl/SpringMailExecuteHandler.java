/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.dto.UploadFileDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.property.MailProperty.MailConfig;
import com.iwindplus.base.mail.domain.vo.MailVO;
import com.iwindplus.base.mail.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.mail.support.MailExecuteHandler;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import reactor.core.publisher.Mono;

/**
 * spring实现邮件业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class SpringMailExecuteHandler extends AbstractBaseServiceImpl<MailConfig> implements MailExecuteHandler {

    /**
     * 构造函数.
     *
     * @param config 邮件配置
     */
    public SpringMailExecuteHandler(MailConfig config) {
        super.setConfig(config);
    }

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

        return Mono.defer(() -> {
            // 业务流水号（延迟到订阅时生成）
            if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
                entity.setBizNumber(this.buildBizNumber(entity));
            }
            return sendOnce(entity);
        });
    }

    private Mono<MailVO> sendOnce(MailDTO dto) {
        final String username = super.getConfig().getUsername();
        final String nickName = super.getConfig().getNickName();
        return doSend(dto)
            .then(Mono.just(MailVO.ok(dto.getBizNumber(),
                username, nickName, 1)))
            .onErrorResume(ex -> {
                final Throwable cause = ExceptionUtils.getRootCause(ex);
                String msg = "邮件发送失败：" + (cause != null ? cause : ex).getMessage();
                log.error(msg, ex);

                return Mono.just(MailVO.fail(dto.getBizNumber(), username, nickName, 1, msg));
            });
    }

    private Mono<Void> doSend(MailDTO dto) {
        return Mono.fromRunnable(() -> {
            try {
                sendMailMessage(dto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void sendMailMessage(MailDTO entity) throws Exception {
        JavaMailSenderImpl sender = buildSender();
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, super.getConfig().getDefaultEncoding().name());

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
        if (CharSequenceUtil.isNotBlank(super.getConfig().getNickName())) {
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
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        final MailConfig config = super.getConfig();

        mailSender.setHost(CharSequenceUtil.trimToNull(config.getHost()));
        mailSender.setPort(config.getPort());
        mailSender.setUsername(CharSequenceUtil.trimToNull(config.getUsername()));
        mailSender.setPassword(CharSequenceUtil.trimToNull(config.getPassword()));
        mailSender.setProtocol(CharSequenceUtil.trimToNull(config.getProtocol()));
        mailSender.setDefaultEncoding(config.getDefaultEncoding().name());

        if (MapUtil.isNotEmpty(config.getProperties())) {
            final Properties properties = new Properties();
            properties.putAll(config.getProperties());
            mailSender.setJavaMailProperties(properties);
        }

        return mailSender;
    }

    private String buildBizNumber(MailDTO entity) {
        String param = new StringBuilder()
            .append(entity.getSubject())
            .append(entity.getContent())
            .append(String.join(",", entity.getTos()))
            .toString();
        return CryptoUtil.encryptBySm3(param);
    }

    private void addAttachments(MimeMessageHelper helper, List<UploadFileDTO> attachments) throws MessagingException {
        if (CollUtil.isEmpty(attachments)) {
            return;
        }

        for (UploadFileDTO attachment : attachments) {
            final String filename = attachment.getSourceFileName();

            byte[] data = attachment.getData();
            if (ArrayUtil.isEmpty(data)) {
                final String url = attachment.getUrl();
                if (CharSequenceUtil.isBlank(url)) {
                    log.warn("附件 [{}] 数据为空且 URL 为空，跳过添加", filename);
                    continue;
                }

                data = HttpsUtil.downloadBytes(url);
                if (ArrayUtil.isEmpty(data)) {
                    log.warn("附件 [{}] 从 URL 下载数据为空，跳过添加", filename);
                    continue;
                }
            }

            final String contentType = CharSequenceUtil.isNotBlank(attachment.getContentType())
                ? attachment.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            helper.addAttachment(
                filename,
                new ByteArrayResource(data, contentType)
            );
        }
    }

}
