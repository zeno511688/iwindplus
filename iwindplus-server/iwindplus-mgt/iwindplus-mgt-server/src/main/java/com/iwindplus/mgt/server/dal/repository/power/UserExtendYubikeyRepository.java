/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import com.iwindplus.mgt.server.dal.mapper.power.UserExtendYubikeyMapper;
import com.iwindplus.mgt.server.dal.model.power.UserExtendYubikeyDO;
import org.springframework.stereotype.Repository;

/**
 * 用户扩展yubikey聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserExtendYubikeyRepository extends JoinCrudRepository<UserExtendYubikeyMapper, UserExtendYubikeyDO> {

    /**
     * 获取yubikey是否已存在.
     *
     * @param userId  用户主键
     * @param bizType 业务类型
     */
    public void getYubikeyIsExist(Long userId, YubikeyBizTypeEnum bizType) {
        final LambdaQueryWrapper<UserExtendYubikeyDO> queryWrapper = Wrappers.lambdaQuery(UserExtendYubikeyDO.class)
            .eq(UserExtendYubikeyDO::getUserId, userId)
            .eq(UserExtendYubikeyDO::getBizType, bizType);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.YUBIKEY_CONFIG_EXIST);
        }
    }

    /**
     * 获取用户扩展yubikey.
     *
     * @param userId  用户主键
     * @param bizType 业务类型
     */
    public UserExtendYubikeyDO getByUserId(Long userId, YubikeyBizTypeEnum bizType) {
        final LambdaQueryWrapper<UserExtendYubikeyDO> queryWrapper = Wrappers.lambdaQuery(UserExtendYubikeyDO.class)
            .eq(UserExtendYubikeyDO::getUserId, userId)
            .eq(UserExtendYubikeyDO::getBizType, bizType);
        return super.getOne(queryWrapper);
    }
}
