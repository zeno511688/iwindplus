/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.im.api.dto.WsMsgDTO;
import com.iwindplus.im.client.WsMsgClient;
import com.iwindplus.im.common.enums.MsgTypeEnum;
import com.iwindplus.im.common.enums.SubMsgTypeEnum;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseExtendVO;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseVO;
import com.iwindplus.mgt.api.upms.vo.RoleBaseVO;
import com.iwindplus.mgt.application.query.upms.permission.ResourceQueryService;
import com.iwindplus.mgt.application.query.upms.permission.RoleQueryService;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WsPushApplicationService {

    private final RoleQueryService roleQueryService;
    private final ResourceQueryService resourceQueryService;
    private final WsMsgClient wsMsgClient;

    public void sendWsRolePermission(Long orgId, Long userId, Long sendOrgId, Long sendUserId) {
        List<RoleBaseVO> listRolePermission = this.roleQueryService.listCheckedByUserId(orgId, userId);
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

    public void sendWsResourcePermission(Long orgId, Long userId, Long sendOrgId, Long sendUserId) {
        List<ResourceBaseExtendVO> listResourcePermission = this.resourceQueryService.listApiCheckedByUserId(orgId, userId);
        if (CollUtil.isEmpty(listResourcePermission)) {
            return;
        }

        final Set<ResourceBaseVO> list = listResourcePermission.stream().sorted(Comparator.comparing(ResourceBaseVO::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        final WsMsgDTO wsMsg = WsMsgDTO.builder()
            .msgType(MsgTypeEnum.TEXT)
            .subMsgType(SubMsgTypeEnum.REFRESH_RESOURCE_PERMISSION.getValue())
            .title(SubMsgTypeEnum.REFRESH_RESOURCE_PERMISSION.getDesc())
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
