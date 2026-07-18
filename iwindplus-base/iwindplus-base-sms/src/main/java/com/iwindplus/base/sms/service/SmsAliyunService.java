/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service;

import com.iwindplus.base.sms.domain.vo.SmsBatchVO;

import java.util.List;

/**
 * 阿里云短信业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface SmsAliyunService extends SmsBaseService {

    /**
     * 发送短信，成功返回流水号.
     *
     * @param phoneNumbers         手机号集合（必填）
     * @param templateParams       模板参数，用于替换短信模板中的参数（可选）
     * @param smsUpExtendCode      上行短信扩展码，上行短信，指发送给通信服务提供商的短信，用于定制某种服务、完成查询，或是办理某种业务等，需要收费的，按运营商普通短信资费进行扣费。（可选）
     * @param phoneNumberGroupSize 每个分组的手机个数（可选，默认：100）
     * @return List<SmsBatchVO>
     */
    List<SmsBatchVO> smsSend(List<String> phoneNumbers, List<String> templateParams, String smsUpExtendCode, Integer phoneNumberGroupSize);
}
