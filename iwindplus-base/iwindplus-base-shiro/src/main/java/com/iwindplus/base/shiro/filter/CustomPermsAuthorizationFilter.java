/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.filter;

import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * 权限过滤器（或者关系）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class CustomPermsAuthorizationFilter extends AuthorizationFilter {

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
        throws IOException {
        Subject subject = getSubject(request, response);
        if (mappedValue instanceof String[] array) {
            if (ArrayUtil.isEmpty(array)) {
                return true;
            }
            return Stream.of(array).anyMatch(subject::isPermitted);
        }
        return false;
    }
}
