/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.service;

import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.vo.MailVO;
import reactor.core.publisher.Mono;

/**
 * 邮箱业务层接口类.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
public interface MailService extends MailBaseService {

    /**
     * 发送邮件.
     *
     * @param entity 邮件对象
     * @return Mono<MailVO>
     */
    Mono<MailVO> send(MailDTO entity);
}
