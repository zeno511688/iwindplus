/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.vo.MailVO;
import com.iwindplus.base.mail.factory.MailExecuteHandlerFactory;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.SecureRandomUtil;
import com.iwindplus.base.util.TemplateUtil;
import com.iwindplus.integr.application.query.MailTplQueryService;
import com.iwindplus.integr.api.dto.MailboxSendDTO;
import com.iwindplus.integr.application.query.vo.MailTplVO;
import com.iwindplus.log.api.dto.MailCaptchaLogDTO;
import com.iwindplus.log.api.dto.MailLogDTO;
import com.iwindplus.log.api.dto.MailSendValidDTO;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.log.client.MailLogClient;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.api.upms.vo.UserInfoVO;
import com.iwindplus.mgt.api.upms.vo.UserVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailboxApplicationService {

    private final MailExecuteHandlerFactory mailExecuteHandlerFactory;
    private final MailTplQueryService mailTplQueryService;
    private final MailLogClient mailLogClient;
    private final MailCaptchaLogClient mailCaptchaLogClient;
    private final UserClient userClient;

    /**
     * 发送邮件.
     *
     * @param entity 对象
     */
    public void send(MailboxSendDTO entity) {
        this.sendMail(entity);
    }

    /**
     * 发送邮箱验证码（邮箱）.
     *
     * @param tplCode 模板配置编码（必填）
     * @param mail    邮箱（必填）
     */
    public void sendCaptcha(String tplCode, String mail) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mail(mail).build();
        final ResultVO<UserInfoVO> userResponse = this.userClient.getLoginInfoByCondition(entity);
        userResponse.errorThrow();
        final UserInfoVO user = userResponse.getBizData();
        final Long userId = user.getUserId();
        final Long orgId = user.getOrgId();

        this.sendCaptcha(tplCode, mail, userId, orgId);
    }

    /**
     * 发送邮箱验证码（用户主键）.
     *
     * @param tplCode 模板配置编码（必填）
     * @param userId  用户主键（必填）
     * @param orgId   组织主键（必填）
     */
    public void sendCaptchaByUserId(String tplCode, Long userId, Long orgId) {
        final ResultVO<UserVO> userResponse = this.userClient.getDetail(userId);
        userResponse.errorThrow();
        final UserVO user = userResponse.getBizData();
        final String mail = user.getMail();

        this.sendCaptcha(tplCode, mail, userId, orgId);
    }

    private void saveMailLog(MailboxSendDTO entity, MailVO data) {
        if (Objects.isNull(entity) || Objects.isNull(data)) {
            return;
        }
        // 保存日志
        MailLogDTO param = MailLogDTO.builder()
            .subject(entity.getSubject())
            .content(entity.getContent())
            .username(data.getUsername())
            .nickName(data.getNickName())
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

    private void sendMail(MailboxSendDTO dto) {
        MailDTO entity = MailDTO.builder()
            .subject(dto.getSubject())
            .content(dto.getContent())
            .tos(dto.getTos())
            .ccs(dto.getCcs())
            .bccs(dto.getBccs())
            .attachments(dto.getAttachments())
            .build();

        this.mailExecuteHandlerFactory.send(entity)
            .doOnNext(data -> {
                log.info("发送邮件结果={}", data);
                saveMailLog(dto, data);
            })
            .subscribe();
    }

    private void sendCaptcha(String tplCode, String mail, Long userId, Long orgId) {
        final MailTplVO mailTpl = this.mailTplQueryService.getByCode(tplCode);
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
        cond.setTos(List.of(mail));
        String captcha = SecureRandomUtil.randomNumbers(6);
        Integer timeout = Objects.isNull(mailTpl.getCaptchaTimeout()) ? SmsConstant.CAPTCHA_TIMEOUT : mailTpl.getCaptchaTimeout();
        cond.setSubject(mailTpl.getName());
        List<String> templateParams = List.of(captcha, timeout.toString());
        String templateContent = TemplateUtil.getTemplateContent(mailTpl.getTemplateContent(), templateParams);
        cond.setContent(templateContent);

        this.mailExecuteHandlerFactory.send(cond)
            .doOnNext(data -> {
                log.info("发送邮件结果={}", data);
                if (ObjectUtil.isNotEmpty(data) && data.getResult()) {
                    long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(timeout);

                    MailCaptchaLogDTO param = MailCaptchaLogDTO.builder()
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
            }).subscribe();
    }

}
