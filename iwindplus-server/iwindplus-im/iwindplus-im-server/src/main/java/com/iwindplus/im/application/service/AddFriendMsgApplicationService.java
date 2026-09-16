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
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.im.application.service.dto.AddFriendMsgDTO;
import com.iwindplus.im.common.enums.MsgStatusEnum;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgRepository;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 加好友消息业务层接口类.
 *
 * @author zengdegui
 * @since 202Join25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddFriendMsgApplicationService {

    private final AddFriendMsgRepository addFriendMsgRepository;
    private final UserClient userClient;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(AddFriendMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        final Long receiverId = entity.getReceiverId();
        List<Long> ids = List.of(userId, receiverId);
        final List<UserExtendVO> list = Optional.ofNullable(this.userClient.listExtendByIds(ids))
            .map(ResultVO::getBizData).orElse(null);
        if (ObjectUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final UserExtendVO currentUserInfo = list.stream()
            .filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.getId(), userId))
            .findFirst().orElse(null);
        String avatar = currentUserInfo.getAvatar();
        String nickName = currentUserInfo.getNickName();
        entity.setMsgStatus(MsgStatusEnum.UN_READ);
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);

        final UserExtendVO receiverUserInfo = list.stream()
            .filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.getId(), receiverId))
            .findFirst().orElse(null);
        entity.setReceiverAvatar(receiverUserInfo.getAvatar());
        entity.setReceiverNickName(receiverUserInfo.getNickName());
        entity.setOrgId(orgId);
        final AddFriendMsgDO model = BeanUtil.copyProperties(entity, AddFriendMsgDO.class);
        this.addFriendMsgRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<String> ids) {
        List<AddFriendMsgDO> list = this.addFriendMsgRepository.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.addFriendMsgRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(AddFriendMsgDTO entity) {
        AddFriendMsgDO data = this.addFriendMsgRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final AddFriendMsgDO model = BeanUtil.copyProperties(entity, AddFriendMsgDO.class);
        return this.addFriendMsgRepository.updateById(model);
    }

    /**
     * 编辑消息状态.
     *
     * @param id        主键
     * @param msgStatus 消息状态
     * @return boolean
     */
    public boolean editMsgStatus(String id, MsgStatusEnum msgStatus) {
        AddFriendMsgDO data = this.addFriendMsgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        AddFriendMsgDO entity = AddFriendMsgDO.builder()
            .id(id)
            .msgStatus(msgStatus)
            .build();
        if (MsgStatusEnum.READ.equals(msgStatus)) {
            entity.setReadTime(System.currentTimeMillis());
        }
        this.addFriendMsgRepository.updateById(entity);
        return Boolean.TRUE;
    }
}
