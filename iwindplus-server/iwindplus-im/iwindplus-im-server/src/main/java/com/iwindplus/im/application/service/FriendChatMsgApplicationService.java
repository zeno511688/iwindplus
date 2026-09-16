/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.im.application.service.dto.FriendChatMsgDTO;
import com.iwindplus.im.common.enums.ImCodeEnum;
import com.iwindplus.im.common.enums.MsgStatusEnum;
import com.iwindplus.im.infrastructure.persistence.es.FriendChatMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.FriendChatMsgRepository;
import com.iwindplus.im.infrastructure.persistence.mysql.UserFriendDO;
import com.iwindplus.im.infrastructure.persistence.mysql.UserFriendRepository;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 好友聊天消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendChatMsgApplicationService {

    private final FriendChatMsgRepository friendChatMsgRepository;
    private final UserClient userClient;
    private final UserFriendRepository userFriendRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(FriendChatMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        final long count = this.userFriendRepository.count(Wrappers.lambdaQuery(UserFriendDO.class)
            .eq(UserFriendDO::getFriendId, entity.getReceiverId())
            .eq(UserFriendDO::getOrgId, orgId)
            .eq(UserFriendDO::getUserId, userId));
        if (!SqlHelper.retBool(count)) {
            throw new BizException(ImCodeEnum.NOT_YOUR_FRIEND);
        }

        final Long receiverId = entity.getReceiverId();
        List<Long> ids = List.of(userId, receiverId);
        final List<UserExtendVO> list = Optional.ofNullable(this.userClient.listExtendByIds(ids))
            .map(ResultVO::getBizData).orElse(null);
        if (ObjectUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final UserExtendVO currentUserInfo = list.stream().filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.getId(), userId)).findFirst().orElse(null);
        String avatar = currentUserInfo.getAvatar();
        String nickName = currentUserInfo.getNickName();
        entity.setMsgStatus(MsgStatusEnum.UN_READ);
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);

        final UserExtendVO receiverUserInfo = list.stream().filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.getId(), receiverId)).findFirst().orElse(null);
        entity.setReceiverAvatar(receiverUserInfo.getAvatar());
        entity.setReceiverNickName(receiverUserInfo.getNickName());
        entity.setOrgId(orgId);
        final FriendChatMsgDO model = BeanUtil.copyProperties(entity, FriendChatMsgDO.class);
        this.friendChatMsgRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(FriendChatMsgDTO entity) {
        FriendChatMsgDO data = this.friendChatMsgRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final FriendChatMsgDO model = BeanUtil.copyProperties(entity, FriendChatMsgDO.class);
        return this.friendChatMsgRepository.updateById(model);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<String> ids) {
        List<FriendChatMsgDO> list = this.friendChatMsgRepository.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.friendChatMsgRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    /**
     * 编辑消息状态.
     *
     * @param id        主键
     * @param msgStatus 消息状态
     * @return boolean
     */
    public boolean editMsgStatus(String id, MsgStatusEnum msgStatus) {
        FriendChatMsgDO data = this.friendChatMsgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FriendChatMsgDO entity = FriendChatMsgDO.builder()
            .id(id)
            .msgStatus(msgStatus)
            .build();
        if (MsgStatusEnum.READ.equals(msgStatus)) {
            entity.setReadTime(System.currentTimeMillis());
        }
        this.friendChatMsgRepository.updateById(entity);
        return Boolean.TRUE;
    }
}
