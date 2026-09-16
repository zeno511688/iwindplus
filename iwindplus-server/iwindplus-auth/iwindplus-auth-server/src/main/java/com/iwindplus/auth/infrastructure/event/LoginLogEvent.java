/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.event;

import com.iwindplus.log.api.dto.LoginLogDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 登录日志事件.
 *
 * @author zengdegui
 * @since 2025/03/22 01:08
 */
@Getter
public class LoginLogEvent extends ApplicationEvent {

    private LoginLogDTO logData;

    public LoginLogEvent(Object source, LoginLogDTO logData) {
        super(source);
        this.logData = logData;
    }
}
