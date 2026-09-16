/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.support;

import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.vo.MailVO;
import com.iwindplus.base.mail.service.BaseService;
import reactor.core.publisher.Mono;

/**
 * 邮件策略标识接口.
 *
 * @author zengdegui
 * @since 2024/9/1
 */
public interface MailExecuteHandler extends BaseService {

    /**
     * 发送邮件.
     *
     * @param entity 邮件对象
     * @return Mono<MailVO>
     */
    Mono<MailVO> send(MailDTO entity);
}
