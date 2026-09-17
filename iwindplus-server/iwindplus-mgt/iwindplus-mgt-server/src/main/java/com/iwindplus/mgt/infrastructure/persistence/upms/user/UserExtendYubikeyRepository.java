/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.upms.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.upms.user.dto.UserExtendYubikeySearchDTO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendYubikeyPageVO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.YubikeyBizTypeEnum;
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
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<UserExtendYubikeyPageVO> pageByUserId(UserExtendYubikeySearchDTO entity) {
        final PageDTO<UserExtendYubikeyDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<UserExtendYubikeyDO> queryWrapper = Wrappers.lambdaQuery(UserExtendYubikeyDO.class)
            .eq(UserExtendYubikeyDO::getUserId, entity.getUserId());
        return super.page(page, queryWrapper)
            .convert(model -> BeanUtil.copyProperties(model, UserExtendYubikeyPageVO.class));
    }

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
