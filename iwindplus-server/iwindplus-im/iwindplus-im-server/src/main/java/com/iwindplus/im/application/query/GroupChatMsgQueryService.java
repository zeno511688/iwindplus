/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.im.application.query.dto.GroupChatMsgSearchDTO;
import com.iwindplus.im.application.query.vo.GroupChatMsgPageVO;
import com.iwindplus.im.application.query.vo.GroupChatMsgVO;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.configuration.ImProperty;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgRepository;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 群聊消息查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatMsgQueryService {

    private final GroupChatMsgRepository groupChatMsgRepository;
    private final OssClient ossClient;
    private final UserClient userClient;
    private final ImProperty property;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<GroupChatMsgPageVO>
     */
    public IPage<GroupChatMsgPageVO> page(PageDTO<GroupChatMsgDO> page, GroupChatMsgSearchDTO entity) {
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<GroupChatMsgDO> wrapper = EsWrappers.<GroupChatMsgDO>lambdaQuery()
            .eq(GroupChatMsgDO::getOrgId, orgId)
            .eq(GroupChatMsgDO::getChatGroupId, entity.getChatGroupId())
            .eq(GroupChatMsgDO::getSendStatus, entity.getSendStatus());
        if (Objects.nonNull(entity.getSenderId())) {
            wrapper.eq(GroupChatMsgDO::getSenderId, entity.getSenderId());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<GroupChatMsgDO> modelPage = this.groupChatMsgRepository.page(page, wrapper);
        final IPage<GroupChatMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, GroupChatMsgPageVO.class));
        this.buildUserPic(result.getRecords());
        return result;
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @return GroupChatMsgVO
     */
    public GroupChatMsgVO getDetail(String id) {
        GroupChatMsgDO data = this.groupChatMsgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getSenderAvatar())) {
            relativePaths.add(data.getSenderAvatar());
        }
        List<FilePathVO> filePaths = this.getFilePaths(
            property.getOss().getCode(),
            property.getOss().getTplCode(), relativePaths, this.ossClient);
        final GroupChatMsgVO result = BeanUtil.copyProperties(data, GroupChatMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    private void buildUserPic(List<GroupChatMsgPageVO> records) {
        if (CollUtil.isNotEmpty(records)) {
            List<String> senderAvatars = records.stream().filter(Objects::nonNull)
                .filter(m -> CharSequenceUtil.isNotBlank(m.getSenderAvatar()))
                .map(GroupChatMsgPageVO::getSenderAvatar).distinct().collect(Collectors.toCollection(ArrayList::new));
            List<String> relativePaths = Lists.newArrayList();
            if (CollUtil.isNotEmpty(senderAvatars)) {
                relativePaths.addAll(senderAvatars);
            }

            List<FilePathVO> filePaths = Optional.ofNullable(
                    this.userClient.listUserOssSignUrl(relativePaths, null))
                .map(ResultVO::getBizData).orElse(null);
            records.forEach(m -> this.buildUserInfo(m, filePaths));
        }
    }

    private void buildUserInfo(GroupChatMsgPageVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
        });
    }

    private void buildUserInfo(GroupChatMsgVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
        });
    }

    private List<FilePathVO> getFilePaths(String ossCode, String ossTplCode, List<String> relativePaths, OssClient ossClient) {
        if (CharSequenceUtil.isNotBlank(ossTplCode) && CollUtil.isNotEmpty(relativePaths)) {
            try {
                return Optional.ofNullable(ossClient.listSignUrl(ossCode, ossTplCode, relativePaths, null))
                    .map(ResultVO::getBizData).orElse(null);
            } catch (Exception ex) {
                log.warn(ExceptionConstant.EXCEPTION, ex);
            }
        }
        return null;
    }
}
