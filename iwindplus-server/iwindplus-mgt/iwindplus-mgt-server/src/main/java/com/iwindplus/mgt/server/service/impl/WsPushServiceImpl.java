/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.im.client.WsMsgClient;
import com.iwindplus.im.domain.dto.WsMsgDTO;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import com.iwindplus.im.domain.enums.SubMsgTypeEnum;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.server.config.property.MgtProperty;
import com.iwindplus.mgt.server.service.WsPushService;
import com.iwindplus.mgt.server.service.power.ResourceService;
import com.iwindplus.mgt.server.service.power.RoleService;
import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * websocket推送业务层接口实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 18:30
 */
@Slf4j
@Service
public class WsPushServiceImpl implements WsPushService {

    @Resource
    private MgtProperty mgtProperty;

    @Resource
    private RoleService roleService;

    @Resource
    private ResourceService resourceService;

    @Resource
    private WsMsgClient wsMsgClient;

    @Override
    public void sendWsRolePermission(Long orgId, Long userId, Long sendOrgId, Long sendUserId) {
        if (Boolean.FALSE.equals(this.mgtProperty.getWs().getEnabled())) {
            return;
        }

        List<RoleBaseVO> listRolePermission = this.roleService.listCheckedByUserId(orgId, userId);
        if (CollUtil.isEmpty(listRolePermission)) {
            return;
        }

        final Set<RoleBaseVO> list = listRolePermission.stream().sorted(Comparator.comparing(RoleBaseVO::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        final WsMsgDTO wsMsg = WsMsgDTO.builder()
            .msgType(MsgTypeEnum.TEXT)
            .subMsgType(SubMsgTypeEnum.REFRESH_ROLE_PERMISSION.getValue())
            .title(SubMsgTypeEnum.REFRESH_ROLE_PERMISSION.getDesc())
            .content(list)
            .receiverId(userId)
            .sendUserId(sendUserId)
            .sendOrgId(sendOrgId)
            .build();
        try {
            this.wsMsgClient.sendPersonNoticeMsg(wsMsg);
        } catch (Exception ex) {
            log.warn("推送消息通知角色权限变更", ex);
        }
    }

    @Override
    public void sendWsButtonPermission(Long orgId, Long userId, Long sendOrgId, Long sendUserId) {
        if (Boolean.FALSE.equals(this.mgtProperty.getWs().getEnabled())) {
            return;
        }

        List<ResourceBaseVO> listButtonPermission = this.resourceService.listButtonCheckedByUserId(orgId, userId);
        if (CollUtil.isEmpty(listButtonPermission)) {
            return;
        }

        final Set<ResourceBaseVO> list = listButtonPermission.stream().sorted(Comparator.comparing(ResourceBaseVO::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        final WsMsgDTO wsMsg = WsMsgDTO.builder()
            .msgType(MsgTypeEnum.TEXT)
            .subMsgType(SubMsgTypeEnum.REFRESH_BUTTON_PERMISSION.getValue())
            .title(SubMsgTypeEnum.REFRESH_BUTTON_PERMISSION.getDesc())
            .content(list)
            .receiverId(userId)
            .sendUserId(sendUserId)
            .sendOrgId(sendOrgId)
            .build();
        try {
            this.wsMsgClient.sendPersonNoticeMsg(wsMsg);
        } catch (Exception ex) {
            log.warn("推送消息通知按钮权限变更", ex);
        }
    }

}
