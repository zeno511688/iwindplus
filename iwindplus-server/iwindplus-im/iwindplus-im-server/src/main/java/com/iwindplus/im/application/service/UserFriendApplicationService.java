/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.im.application.service.dto.UserFriendDTO;
import com.iwindplus.im.common.enums.FriendStatusEnum;
import com.iwindplus.im.common.enums.ImCodeEnum;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户好友业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserFriendApplicationService {

    private final UserFriendRepository userFriendRepository;
    private final UserClient userClient;

    /**
     * 添加.
     *
     * @param entity   对象
     * @param userInfo 用户信息
     * @return boolean
     */
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
        List<Long> ids = List.of(entity.getFriendId(), entity.getUserId());
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

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<Long> ids) {
        List<UserFriendDO> list = this.userFriendRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.userFriendRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(UserFriendDTO entity) {
        UserFriendDO data = this.userFriendRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final UserFriendDO model = BeanUtil.copyProperties(entity, UserFriendDO.class);
        this.userFriendRepository.updateById(model);
        return Boolean.TRUE;
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
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
}
