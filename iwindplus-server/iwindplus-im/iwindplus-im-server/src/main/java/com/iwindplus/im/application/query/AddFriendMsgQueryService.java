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
import com.iwindplus.im.application.query.dto.AddFriendMsgSearchDTO;
import com.iwindplus.im.application.query.vo.AddFriendMsgPageVO;
import com.iwindplus.im.application.query.vo.AddFriendMsgVO;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.configuration.ImProperty;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgRepository;
import com.iwindplus.integr.client.OssClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 加好友消息查询业务层.
 *
 * @author zengdegui
 * @since 202Join25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddFriendMsgQueryService {

    private final OssClient ossClient;
    private final ImProperty property;
    private final AddFriendMsgRepository addFriendMsgRepository;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<AddFriendMsgPageVO>
     */
    public IPage<AddFriendMsgPageVO> page(PageDTO<AddFriendMsgDO> page, AddFriendMsgSearchDTO entity) {
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<AddFriendMsgDO> wrapper = EsWrappers.<AddFriendMsgDO>lambdaQuery()
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
        final IPage<AddFriendMsgDO> modelPage = this.addFriendMsgRepository.page(page, wrapper);
        final IPage<AddFriendMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, AddFriendMsgPageVO.class));
        return result;
    }

    /**
     * 查询未接收到的.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return List<AddFriendMsgVO>
     */
    public List<AddFriendMsgVO> listByUnSendSuccess(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<AddFriendMsgDO> wrapper = EsWrappers.<AddFriendMsgDO>lambdaQuery()
            .eq(AddFriendMsgDO::getOrgId, orgId)
            .eq(AddFriendMsgDO::getReceiverId, userId)
            .ne(AddFriendMsgDO::getSendStatus, SendStatusEnum.SUCCESS);
        final List<AddFriendMsgDO> list = this.addFriendMsgRepository.list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, AddFriendMsgVO.class);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return AddFriendMsgDO
     */
    public AddFriendMsgVO getDetail(String id) {
        AddFriendMsgDO data = this.addFriendMsgRepository.getById(id);
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
        List<FilePathVO> filePaths = this.getFilePaths(
            property.getOss().getCode(), property.getOss().getTplCode(), relativePaths, this.ossClient);
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
