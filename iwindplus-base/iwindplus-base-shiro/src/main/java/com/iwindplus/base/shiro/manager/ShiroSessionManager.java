/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.manager;

import jakarta.servlet.ServletRequest;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * 优化单次请求需要多次访问redis的问题.
 *
 * @author zengdegui
 * @since 2018/9/27
 */
public class ShiroSessionManager extends DefaultWebSessionManager {
    /**
     * 减少多次从redis中读取session.
     *
     * @param sessionKey sessionKey
     * @return Session
     * @throws UnknownSessionException
     */
    @Override
    protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {
        Serializable sessionId = getSessionId(sessionKey);
        ServletRequest request = null;
        if (sessionKey instanceof WebSessionKey obj) {
            request = obj.getServletRequest();
        }
        if (Objects.nonNull(request) && Objects.nonNull(sessionId)) {
            Session session = (Session) request.getAttribute(sessionId.toString());
            if (Objects.nonNull(session)) {
                return session;
            }
            session = super.retrieveSession(sessionKey);
            request.setAttribute(sessionId.toString(), session);
            return session;
        }
        return super.retrieveSession(sessionKey);
    }
}
