/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.service;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.im.application.service.dto.GroupChatMsgDTO;
import com.iwindplus.im.common.enums.ImCodeEnum;
import com.iwindplus.im.infrastructure.configuration.ImProperty;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgRepository;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupDO;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupRepository;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 群聊消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatMsgApplicationService {

    private final GroupChatMsgRepository groupChatMsgRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final OssClient ossClient;
    private final UserClient userClient;
    private final ImProperty property;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(GroupChatMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        List<Long> ids = List.of(userId);
        final UserExtendVO data = Optional.ofNullable(this.userClient.listExtendByIds(ids)).map(ResultVO::getBizData).map(m -> m.get(0)).orElse(null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        String avatar = data.getAvatar();
        String nickName = data.getNickName();
        final ChatGroupDO result = this.chatGroupRepository.getById(entity.getChatGroupId());
        if (Objects.isNull(result)) {
            throw new BizException(ImCodeEnum.CHAT_GROUP_NOT_EXIST);
        }
        entity.setSeq(this.groupChatMsgRepository.getNextSeq(orgId, entity.getChatGroupId()));
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);
        entity.setOrgId(orgId);
        final GroupChatMsgDO model = BeanUtil.copyProperties(entity, GroupChatMsgDO.class);
        this.groupChatMsgRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(GroupChatMsgDTO entity) {
        GroupChatMsgDO data = this.groupChatMsgRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final GroupChatMsgDO model = BeanUtil.copyProperties(entity, GroupChatMsgDO.class);
        return this.groupChatMsgRepository.updateById(model);
    }

    /**
     * 通过聊天群主键真实删除.
     *
     * @param chatGroupIds 聊天群主键集合
     * @return boolean
     */
    public boolean removeByChatGroupIds(List<Long> chatGroupIds) {
        return this.groupChatMsgRepository.removeByIds(chatGroupIds, false);
    }

}
