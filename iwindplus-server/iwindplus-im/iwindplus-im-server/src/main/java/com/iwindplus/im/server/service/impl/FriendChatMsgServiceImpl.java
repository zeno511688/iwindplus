/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.im.domain.dto.FriendChatMsgDTO;
import com.iwindplus.im.domain.dto.FriendChatMsgSearchDTO;
import com.iwindplus.im.domain.enums.ImCodeEnum;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.FriendChatMsgPageVO;
import com.iwindplus.im.domain.vo.FriendChatMsgVO;
import com.iwindplus.im.server.dal.model.FriendChatMsgDO;
import com.iwindplus.im.server.dal.model.UserFriendDO;
import com.iwindplus.im.server.dal.repository.UserFriendRepository;
import com.iwindplus.im.server.service.FriendChatMsgService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.setup.client.OssClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 好友聊天消息业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FriendChatMsgServiceImpl extends EsBaseServiceImpl<FriendChatMsgDO> implements FriendChatMsgService {

    private final UserClient userClient;
    private final OssClient ossClient;
    private final UserFriendRepository userFriendRepository;

    @Override
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
        List<Long> ids = Arrays.asList(userId, receiverId);
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
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(FriendChatMsgDTO entity) {
        FriendChatMsgDO data = super.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final FriendChatMsgDO model = BeanUtil.copyProperties(entity, FriendChatMsgDO.class);
        return super.updateById(model);
    }

    @Override
    public List<FriendChatMsgVO> listByUnSendSuccess(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<FriendChatMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(FriendChatMsgDO::getOrgId, orgId)
            .eq(FriendChatMsgDO::getReceiverId, userId)
            .ne(FriendChatMsgDO::getSendStatus, SendStatusEnum.SUCCESS);
        final List<FriendChatMsgDO> list = super.list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, FriendChatMsgVO.class);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        List<FriendChatMsgDO> list = super.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @Override
    public boolean editMsgStatus(String id, MsgStatusEnum msgStatus) {
        FriendChatMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FriendChatMsgDO entity = FriendChatMsgDO.builder()
            .id(id)
            .msgStatus(msgStatus)
            .build();
        if (MsgStatusEnum.READ.equals(msgStatus)) {
            entity.setReadTime(LocalDateTime.now());
        }
        super.updateById(entity);
        return Boolean.TRUE;
    }

    @Override
    public IPage<FriendChatMsgPageVO> page(PageDTO<FriendChatMsgDO> page, FriendChatMsgSearchDTO entity) {
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<FriendChatMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(FriendChatMsgDO::getOrgId, orgId)
            .or(w -> w
                .eq(FriendChatMsgDO::getReceiverId, userId)
                .eq(FriendChatMsgDO::getSenderId, userId)
            )
            .eq(FriendChatMsgDO::getSendStatus, entity.getSendStatus());
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<FriendChatMsgDO> modelPage = super.page(page, wrapper);
        final IPage<FriendChatMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, FriendChatMsgPageVO.class));
        return result;
    }

    @Override
    public FriendChatMsgVO getDetail(String id, String ossTplCode) {
        FriendChatMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getSenderAvatar())) {
            relativePaths.add(data.getSenderAvatar());
        }
        if (CharSequenceUtil.isNotBlank(data.getReceiverAvatar())) {
            relativePaths.add(data.getReceiverAvatar());
        }
        List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
        final FriendChatMsgVO result = BeanUtil.copyProperties(data, FriendChatMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    private void buildUserInfo(FriendChatMsgVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
            if (CharSequenceUtil.isNotBlank(data.getReceiverAvatar()) && data.getReceiverAvatar().equals(p.getRelativePath())) {
                data.setReceiverAvatar(p.getAbsolutePath());
            }
        });
    }
}
