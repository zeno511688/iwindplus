/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service;

/**
 * websocket推送业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface WsPushService {

    /**
     * 推送用户角色权限数据.
     *
     * @param orgId      组织主键
     * @param userId     用户主键
     * @param sendOrgId  发送人组织主键
     * @param sendUserId 发送人组织用户主键
     */
    void sendWsRolePermission(Long orgId, Long userId, Long sendOrgId, Long sendUserId);

    /**
     * 推送用户按钮权限数据.
     *
     * @param orgId      组织主键
     * @param userId     用户主键
     * @param sendOrgId  发送人组织主键
     * @param sendUserId 发送人组织用户主键
     */
    void sendWsButtonPermission(Long orgId, Long userId, Long sendOrgId, Long sendUserId);
}
