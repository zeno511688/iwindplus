/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.im.domain.dto.UserFriendDTO;
import com.iwindplus.im.domain.dto.UserFriendSearchDTO;
import com.iwindplus.im.domain.enums.FriendStatusEnum;
import com.iwindplus.im.domain.enums.ImCodeEnum;
import com.iwindplus.im.domain.vo.UserFriendPageVO;
import com.iwindplus.im.domain.vo.UserFriendVO;
import com.iwindplus.im.server.dal.model.UserFriendDO;
import com.iwindplus.im.server.dal.repository.UserFriendRepository;
import com.iwindplus.im.server.service.UserFriendService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.setup.client.OssClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户好友业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserFriendServiceImpl implements UserFriendService {

    private final UserFriendRepository userFriendRepository;
    private final OssClient ossClient;
    private final UserClient userClient;

    @Override
    public boolean save(UserFriendDTO entity, UserBaseVO userInfo) {
        Long userId = userInfo.getUserId();
        Long orgId = userInfo.getOrgId();
        if (entity.getFriendId().equals(userId)) {
            throw new BizException(ImCodeEnum.NOT_ADD_ONESELF);
        }
        entity.setOrgId(orgId);
        entity.setUserId(userId);
        entity.setStatus(FriendStatusEnum.UN_CONFIRMED);
        // 校验好友是否存在
        this.getFriendIsExist(entity.getFriendId(), userId, orgId);
        List<Long> ids = Arrays.asList(entity.getFriendId(), entity.getUserId());
        final List<UserExtendVO> userList = Optional.ofNullable(this.userClient.listExtendByIds(ids))
            .map(ResultVO::getBizData).orElse(Lists.newArrayList());
        if (CollUtil.isNotEmpty(userList)) {
            userList.forEach(user -> {
                if (user.getId().equals(entity.getUserId())) {
                    entity.setUserAvatar(user.getAvatar());
                    entity.setUserNickName(user.getNickName());
                } else if (user.getId().equals(entity.getFriendId())) {
                    entity.setFriendAvatar(user.getAvatar());
                    entity.setFriendNickName(user.getNickName());
                }
            });
        }
        final UserFriendDO model = BeanUtil.copyProperties(entity, UserFriendDO.class);
        this.userFriendRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        List<UserFriendDO> list = this.userFriendRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.userFriendRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(UserFriendDTO entity) {
        UserFriendDO data = this.userFriendRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final UserFriendDO model = BeanUtil.copyProperties(entity, UserFriendDO.class);
        this.userFriendRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Override
    public boolean editStatus(Long id, FriendStatusEnum status) {
        UserFriendDO data = this.userFriendRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        UserFriendDO param = new UserFriendDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.userFriendRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
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
        queryWrapper.select(UserFriendDO::getId, UserFriendDO::getCreatedTime, UserFriendDO::getCreatedTimestamp, UserFriendDO::getCreatedBy,
            UserFriendDO::getModifiedTime, UserFriendDO::getModifiedTimestamp, UserFriendDO::getModifiedBy,
            UserFriendDO::getVersion, UserFriendDO::getStatus, UserFriendDO::getUserNickName, UserFriendDO::getFriendNickName);
        final IPage<UserFriendDO> modelPage = this.userFriendRepository.page(page, queryWrapper);
        final IPage<UserFriendPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, UserFriendPageVO.class));
        return result;
    }

    private void getFriendIsExist(Long friendId, Long userId, Long orgId) {
        final LambdaQueryWrapper<UserFriendDO> queryWrapper = Wrappers.lambdaQuery(UserFriendDO.class);
        queryWrapper.eq(UserFriendDO::getOrgId, orgId);
        queryWrapper.eq(UserFriendDO::getUserId, userId);
        queryWrapper.eq(UserFriendDO::getFriendId, friendId);
        boolean result = SqlHelper.retBool(this.userFriendRepository.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(ImCodeEnum.FRIEND_EXIST);
        }
    }

    private void buildUserPic(List<UserFriendVO> records, String ossTplCode) {
        if (CollUtil.isNotEmpty(records) && CharSequenceUtil.isNotBlank(ossTplCode)) {
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
            List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
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
