/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.shiro.domain.vo.ShiroUserVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.util.Optional;

/**
 * shiro工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public class ShiroUtil {

    private ShiroUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * Subject：主体，代表了当前用户.
     *
     * @return Subject
     */
    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    /**
     * 获取用户信息.
     *
     * @return ShiroUserVO
     */
    public static ShiroUserVO getUserInfo() {
        return Optional.ofNullable(getSubject().getPrincipal()).map(p -> (ShiroUserVO) p).orElse(null);
    }

    /**
     * 获取用户主键.
     *
     * @return Long
     */
    public static Long getUserId() {
        return Optional.ofNullable(getUserInfo()).map(ShiroUserVO::getUserId).orElse(null);
    }
}
