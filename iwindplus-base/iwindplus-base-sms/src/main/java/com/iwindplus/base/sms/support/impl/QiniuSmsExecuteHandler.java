/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.support.impl;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.sms.domain.constant.SmsConstant.QiniuConstant;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.property.SmsProperty.QiniuConfig;
import com.iwindplus.base.sms.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.sms.support.SmsExecuteHandler;
import com.iwindplus.base.util.IosUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.TemplateUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.sms.model.TemplateInfo;
import com.qiniu.util.Auth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 七牛云短信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
public class QiniuSmsExecuteHandler extends AbstractBaseServiceImpl<QiniuConfig> implements SmsExecuteHandler {

    /**
     * 构造函数.
     *
     * @param config 七牛云短信配置
     */
    public QiniuSmsExecuteHandler(QiniuConfig config) {
        this.setConfig(config);
    }

    @Override
    public SmsTypeEnum getProvider() {
        return SmsTypeEnum.QINIU;
    }

    @Override
    protected String sendSms(SmsSendRequestDTO entity) throws QiniuException {
        final List<String> phoneNumbers = entity.getPhoneNumbers();
        Auth auth = Auth.create(this.getConfig().getAccessKey(), this.getConfig().getSecretKey());
        SmsManager smsManager = new SmsManager(auth);
        String[] mobiles = phoneNumbers.stream().toArray(String[]::new);
        Response response = null;
        try {
            TemplateInfo.Item describeTemplateItem = smsManager.describeTemplateItem(entity.getTemplateContent());
            if (Objects.nonNull(describeTemplateItem)) {
                Map<String, String> templateParam = TemplateUtil.getTemplateParam(describeTemplateItem.getTemplate(), entity.getTemplateParams());
                if (MapUtil.isNotEmpty(templateParam)) {
                    response = smsManager.sendMessage(entity.getTemplateContent(), mobiles, templateParam);
                } else {
                    response = smsManager.sendFulltextMessage(mobiles, describeTemplateItem.getTemplate());
                }
            }
            if (Objects.nonNull(response) && response.isOK()) {
                JsonNode data = JacksonUtil.parseTree(response.bodyString());
                return Optional.ofNullable(data)
                    .map(p -> p.get(QiniuConstant.BIZ_NUMBER))
                    .map(JsonNode::asText)
                    .orElse(null);
            }
        } finally {
            this.closeResponse(response);
        }
        return null;
    }

    private void closeResponse(Response response) {
        IosUtil.closeQuietly(response, Response::close);
    }
}