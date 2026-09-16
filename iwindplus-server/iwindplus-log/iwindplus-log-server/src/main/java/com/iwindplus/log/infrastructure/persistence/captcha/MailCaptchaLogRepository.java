/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.infrastructure.persistence.captcha;

import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import org.springframework.stereotype.Repository;

/**
 * 邮箱验证码日志查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:50
 */
@Repository
public class MailCaptchaLogRepository extends EsBaseServiceImpl<MailCaptchaLogDO> implements EsBaseService<MailCaptchaLogDO> {

}
