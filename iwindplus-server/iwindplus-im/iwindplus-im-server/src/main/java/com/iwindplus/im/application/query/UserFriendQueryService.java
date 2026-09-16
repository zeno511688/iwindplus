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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.im.application.query.dto.UserFriendSearchDTO;
import com.iwindplus.im.application.query.vo.UserFriendPageVO;
import com.iwindplus.im.application.query.vo.UserFriendVO;
import com.iwindplus.im.common.enums.FriendStatusEnum;
import com.iwindplus.im.infrastructure.persistence.mysql.UserFriendDO;
import com.iwindplus.im.infrastructure.persistence.mysql.UserFriendRepository;
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
 * 用户好友查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFriendQueryService {

    private final UserFriendRepository userFriendRepository;
    private final UserClient userClient;

    /**
     * 分页查询.
     *
     * @param page 分页参数
     * @param entity 查询参数
     * @return 分页结果
     */
    public IPage<UserFriendPageVO> page(PageDTO<UserFriendDO> page, UserFriendSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        final LambdaQueryWrapper<UserFriendDO> queryWrapper = Wrappers.lambdaQuery(UserFriendDO.class);
        queryWrapper.eq(UserFriendDO::getOrgId, orgId);
        queryWrapper.eq(UserFriendDO::getUserId, userId);
        if (Objects.isNull(entity.getStatus())) {
            queryWrapper.eq(UserFriendDO::getStatus, FriendStatusEnum.PASSED);
        }
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.asc("friendNickName");
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(UserFriendDO::getId, UserFriendDO::getCreatedTimestamp, UserFriendDO::getCreatedBy,
            UserFriendDO::getModifiedTimestamp, UserFriendDO::getModifiedBy,
            UserFriendDO::getVersion, UserFriendDO::getStatus, UserFriendDO::getUserNickName, UserFriendDO::getFriendNickName);
        final IPage<UserFriendDO> modelPage = this.userFriendRepository.page(page, queryWrapper);
        final IPage<UserFriendPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, UserFriendPageVO.class));
        return result;
    }

    private void buildUserPic(List<UserFriendVO> records) {
        if (CollUtil.isNotEmpty(records)) {
            List<String> userAvatars = records.stream().filter(Objects::nonNull)
                .filter(m -> CharSequenceUtil.isNotBlank(m.getUserAvatar()))
                .map(UserFriendVO::getUserAvatar).distinct().collect(Collectors.toCollection(ArrayList::new));
            List<String> friendAvatars = records.stream().filter(Objects::nonNull)
                .filter(m -> CharSequenceUtil.isNotBlank(m.getFriendAvatar()))
                .map(UserFriendVO::getFriendAvatar).distinct().collect(Collectors.toCollection(ArrayList::new));
            List<String> relativePaths = Lists.newArrayList();
            if (CollUtil.isNotEmpty(userAvatars)) {
                relativePaths.addAll(userAvatars);
            }
            if (CollUtil.isNotEmpty(friendAvatars)) {
                relativePaths.addAll(friendAvatars);
            }
            List<FilePathVO> filePaths = Optional.ofNullable(
                    this.userClient.listUserOssSignUrl(relativePaths, null))
                .map(ResultVO::getBizData).orElse(null);
            records.forEach(m -> this.buildUserInfo(m, filePaths));
        }
    }

    private void buildUserInfo(UserFriendVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getUserAvatar()) && data.getUserAvatar().equals(p.getRelativePath())) {
                data.setUserAvatar(p.getAbsolutePath());
            }
            if (CharSequenceUtil.isNotBlank(data.getFriendAvatar()) && data.getFriendAvatar().equals(p.getRelativePath())) {
                data.setFriendAvatar(p.getAbsolutePath());
            }
        });
    }
}
