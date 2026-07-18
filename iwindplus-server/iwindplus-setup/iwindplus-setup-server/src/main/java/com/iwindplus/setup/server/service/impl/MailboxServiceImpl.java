/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.property.MailProperty;
import com.iwindplus.base.mail.domain.vo.MailVO;
import com.iwindplus.base.mail.service.MailService;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.TemplateUtil;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.log.client.MailLogClient;
import com.iwindplus.log.domain.dto.MailCaptchaLogDTO;
import com.iwindplus.log.domain.dto.MailLogDTO;
import com.iwindplus.log.domain.dto.MailSendValidDTO;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import com.iwindplus.mgt.domain.vo.power.UserVO;
import com.iwindplus.setup.domain.dto.MailboxSendDTO;
import com.iwindplus.setup.domain.vo.MailConfigVO;
import com.iwindplus.setup.domain.vo.MailTplVO;
import com.iwindplus.setup.server.dal.model.MailConfigDO;
import com.iwindplus.setup.server.service.MailConfigService;
import com.iwindplus.setup.server.service.MailTplService;
import com.iwindplus.setup.server.service.MailboxService;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.scheduler.Schedulers;

/**
 * 邮箱业务层接口实现类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailboxServiceImpl implements MailboxService {

    private final MailConfigService mailConfigService;
    private final MailTplService mailTplService;
    private final MailService mailService;
    private final MailLogClient mailLogClient;
    private final MailCaptchaLogClient mailCaptchaLogClient;
    private final UserClient userClient;

    @Override
    public void send(MailboxSendDTO entity) {
        MailConfigVO mailConfig = this.getMailConfig(entity.getCode());
        MailConfigDO model = BeanUtil.copyProperties(mailConfig, MailConfigDO.class);

        this.sendMail(entity, model);
    }

    @Override
    public void sendCaptcha(String requestId, String tplCode, String mail) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mail(mail).build();
        final ResultVO<UserInfoVO> userResponse = this.userClient.getLoginInfoByCondition(entity);
        userResponse.errorThrow();
        final UserInfoVO user = userResponse.getBizData();
        final Long userId = user.getUserId();
        final Long orgId = user.getOrgId();

        this.sendCaptcha(requestId, tplCode, mail, userId, orgId);
    }

    @Override
    public void sendCaptchaByUserId(String requestId, String tplCode, Long userId, Long orgId) {
        final ResultVO<UserVO> userResponse = this.userClient.getDetail(userId);
        userResponse.errorThrow();
        final UserVO user = userResponse.getBizData();
        final String mail = user.getMail();

        this.sendCaptcha(requestId, tplCode, mail, userId, orgId);
    }

    private void saveMailLog(MailConfigDO mailConfig, MailboxSendDTO entity, MailVO data) {
        if (Objects.isNull(mailConfig) || Objects.isNull(entity) || Objects.isNull(data)) {
            return;
        }
        // 保存日志
        MailLogDTO param = MailLogDTO.builder()
            .requestId(entity.getRequestId())
            .subject(entity.getSubject())
            .content(entity.getContent())
            .username(mailConfig.getUsername())
            .nickName(mailConfig.getNickName())
            .bizNumber(data.getBizNumber())
            .result(data.getResult())
            .sendCount(data.getSendCount())
            .errorMsg(data.getErrorMsg())
            .build();
        param.setTos(JacksonUtil.toJsonStr(entity.getTos()));
        if (CollUtil.isNotEmpty(entity.getCcs())) {
            param.setCcs(JacksonUtil.toJsonStr(entity.getCcs()));
        }
        if (CollUtil.isNotEmpty(entity.getBccs())) {
            param.setBccs(JacksonUtil.toJsonStr(entity.getBccs()));
        }
        this.mailLogClient.save(param);
    }

    private MailConfigVO getMailConfigById(Long configId) {
        MailConfigVO mailConfig = this.mailConfigService.getDetail(configId);
        this.buildMailConfig(mailConfig);
        return mailConfig;
    }

    private MailConfigVO getMailConfig(String code) {
        MailConfigVO mailConfig = this.mailConfigService.getByCode(code);
        this.buildMailConfig(mailConfig);
        return mailConfig;
    }

    private void buildMailConfig(MailConfigVO mailConfig) {
        MailProperty config = new MailProperty();
        config.setHost(mailConfig.getHost());
        config.setUsername(mailConfig.getUsername());
        config.setPassword(mailConfig.getPassword());
        config.setPort(mailConfig.getPort());
        config.setNickName(mailConfig.getNickName());
        config.setEnableRetry(mailConfig.getRetryEnable());
        config.setDefaultEncoding(Charset.defaultCharset());
        if (Boolean.TRUE.equals(mailConfig.getSslEnable())) {
            Map<String, String> props = new HashMap<>(16);
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", mailConfig.getHost());
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.mime.splitlongparameters", "false");
            config.setProperties(props);
        }
        this.mailService.setConfig(config);
    }

    private void sendMail(MailboxSendDTO dto, MailConfigDO model) {
        MailDTO entity = MailDTO.builder()
            .subject(dto.getSubject())
            .content(dto.getContent())
            .tos(dto.getTos())
            .ccs(dto.getCcs())
            .bccs(dto.getBccs())
            .attachments(dto.getAttachments())
            .build();

        mailService.send(entity)
            .doOnNext(data -> {
                log.info("发送邮件结果={}", data);
                saveMailLog(model, dto, data);
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    private void sendCaptcha(String requestId, String tplCode, String mail, Long userId, Long orgId) {
        final MailTplVO mailTpl = this.mailTplService.getByCode(tplCode);
        MailConfigVO result = this.getMailConfigById(mailTpl.getConfigId());
        final MailSendValidDTO entity = MailSendValidDTO.builder()
            .tplCode(tplCode)
            .userId(userId)
            .orgId(orgId)
            .limitCountDay(mailTpl.getLimitCountDay())
            .limitCountHour(mailTpl.getLimitCountHour())
            .limitCountMinute(mailTpl.getLimitCountMinute())
            .build();
        final ResultVO<Boolean> canSend = this.mailCaptchaLogClient.checkCanSend(entity);
        canSend.errorThrow();

        MailDTO cond = new MailDTO();
        cond.setTos(Arrays.asList(mail));
        String captcha = RandomUtil.randomNumbers(6);
        Integer timeout = Objects.isNull(mailTpl.getCaptchaTimeout()) ? SmsConstant.CAPTCHA_TIMEOUT : mailTpl.getCaptchaTimeout();
        cond.setSubject(mailTpl.getName());
        List<String> templateParams = Arrays.asList(captcha, timeout.toString());
        String templateContent = TemplateUtil.getTemplateContent(mailTpl.getTemplateContent(), templateParams);
        cond.setContent(templateContent);

        mailService.send(cond)
            .doOnNext(data -> {
                log.info("发送邮件结果={}", data);
                if (ObjectUtil.isNotEmpty(data) && data.getResult()) {
                    LocalDateTime expireTime = LocalDateTime.now().plusMinutes(timeout);
                    MailCaptchaLogDTO param = MailCaptchaLogDTO.builder()
                        .requestId(requestId)
                        .bizNumber(data.getBizNumber())
                        .tplCode(tplCode)
                        .mail(mail)
                        .captcha(captcha)
                        .expireTime(expireTime)
                        .userId(userId)
                        .orgId(orgId)
                        .build();
                    this.mailCaptchaLogClient.save(param);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}
