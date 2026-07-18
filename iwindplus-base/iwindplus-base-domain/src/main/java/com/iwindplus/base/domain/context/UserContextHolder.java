/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.vo.UserBaseVO;

/**
 * 用户信息上下文对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public final class UserContextHolder {

    private static final TransmittableThreadLocal<UserBaseVO> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private UserContextHolder() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取.
     *
     * @return UserBaseVO
     */
    public static UserBaseVO getContext() {
        UserBaseVO user = THREAD_LOCAL.get();
        if (user == null) {
            user = getDefaultUser();
            THREAD_LOCAL.set(user);
        }
        return user;
    }

    /**
     * 获取默认用户信息.
     *
     * @return UserBaseVO
     */
    public static UserBaseVO getDefaultUser() {
        return UserBaseVO.builder()
            .userId(0L)
            .orgId(0L)
            .jobNumber(CommonConstant.SystemConstant.SYSTEM)
            .username(CommonConstant.SystemConstant.SYSTEM)
            .mobile(CommonConstant.SystemConstant.SYSTEM)
            .realName(CommonConstant.SystemConstant.SYSTEM)
            .mail(CommonConstant.SystemConstant.SYSTEM)
            .idCard(CommonConstant.SystemConstant.SYSTEM)
            .nickName(CommonConstant.SystemConstant.SYSTEM)
            .build();
    }

    /**
     * 设置.
     *
     * @param entity 对象
     */
    public static void setContext(UserBaseVO entity) {
        THREAD_LOCAL.set(entity);
    }

    /**
     * 强制清空本地线程，防止内存泄漏，如手动调用了set可调用此方法确保清除.
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
