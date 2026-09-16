/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.upms.user.dto.UserExtendYubikeySearchDTO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendYubikeyPageVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendYubikeyVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendYubikeyDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendYubikeyRepository;
import com.iwindplus.mgt.common.enums.YubikeyBizTypeEnum;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 用户扩展yubikey查询业务层.
 *
 * @author zengdegui
 * @since 2026/04/26 20:26
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY})
@RequiredArgsConstructor
public class UserExtendYubikeyQueryService {

    private final UserExtendYubikeyRepository userYubikeyRepository;

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
        return this.userYubikeyRepository.page(page, queryWrapper)
            .convert(model -> BeanUtil.copyProperties(model, UserExtendYubikeyPageVO.class));
    }

    /**
     * 根据ID查询详情.
     *
     * @param id ID
     * @return 详情
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public UserExtendYubikeyVO getDetail(Long id) {
        UserExtendYubikeyDO data = this.userYubikeyRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, UserExtendYubikeyVO.class);
    }

    /**
     * 根据用户ID和业务类型查询详情.
     *
     * @param userId  用户ID
     * @param bizType 业务类型
     * @return 详情
     */
    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    public UserExtendYubikeyVO getByUserId(Long userId, YubikeyBizTypeEnum bizType) {
        UserExtendYubikeyDO data = this.userYubikeyRepository.getOne(Wrappers.lambdaQuery(UserExtendYubikeyDO.class)
            .eq(UserExtendYubikeyDO::getUserId, userId)
            .eq(UserExtendYubikeyDO::getBizType, bizType));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, UserExtendYubikeyVO.class);
    }
}
