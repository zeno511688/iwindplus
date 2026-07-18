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
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.im.domain.dto.AddFriendMsgDTO;
import com.iwindplus.im.domain.dto.AddFriendMsgSearchDTO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.AddFriendMsgPageVO;
import com.iwindplus.im.domain.vo.AddFriendMsgVO;
import com.iwindplus.im.server.dal.model.AddFriendMsgDO;
import com.iwindplus.im.server.service.AddFriendMsgService;
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
 * 加好友消息业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class AddFriendMsgServiceImpl extends EsBaseServiceImpl<AddFriendMsgDO> implements AddFriendMsgService {

    private final UserClient userClient;
    private final OssClient ossClient;

    @Override
    public boolean save(AddFriendMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        final Long receiverId = entity.getReceiverId();
        List<Long> ids = Arrays.asList(userId, receiverId);
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
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(AddFriendMsgDTO entity) {
        AddFriendMsgDO data = super.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final AddFriendMsgDO model = BeanUtil.copyProperties(entity, AddFriendMsgDO.class);
        return super.updateById(model);
    }

    @Override
    public boolean editMsgStatus(String id, MsgStatusEnum msgStatus) {
        AddFriendMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        AddFriendMsgDO entity = AddFriendMsgDO.builder()
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
    public boolean removeByIds(List<String> ids) {
        List<AddFriendMsgDO> list = super.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @Override
    public IPage<AddFriendMsgPageVO> page(PageDTO<AddFriendMsgDO> page, AddFriendMsgSearchDTO entity) {
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<AddFriendMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(AddFriendMsgDO::getOrgId, orgId)
            .or(w -> w
                .eq(AddFriendMsgDO::getReceiverId, userId)
                .eq(AddFriendMsgDO::getSenderId, userId)
            )
            .eq(AddFriendMsgDO::getSendStatus, entity.getSendStatus());
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<AddFriendMsgDO> modelPage = super.page(page, wrapper);
        final IPage<AddFriendMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, AddFriendMsgPageVO.class));
        return result;
    }

    @Override
    public List<AddFriendMsgVO> listByUnSendSuccess(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<AddFriendMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(AddFriendMsgDO::getOrgId, orgId)
            .eq(AddFriendMsgDO::getReceiverId, userId)
            .ne(AddFriendMsgDO::getSendStatus, SendStatusEnum.SUCCESS);
        final List<AddFriendMsgDO> list = super.list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, AddFriendMsgVO.class);
    }

    @Override
    public AddFriendMsgVO getDetail(String id, String ossTplCode) {
        AddFriendMsgDO data = super.getById(id);
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
        final AddFriendMsgVO result = BeanUtil.copyProperties(data, AddFriendMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    private void buildUserInfo(AddFriendMsgVO data, List<FilePathVO> filePaths) {
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
