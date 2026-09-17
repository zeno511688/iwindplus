/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.mgt.application.query.upms.user.dto.UserExtendBindGrantSearchDTO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantPageVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendBindGrantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 用户扩展绑定授权查询业务层.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_BIND_GRANT})
@RequiredArgsConstructor
public class UserExtendBindGrantQueryService {

    private final UserExtendBindGrantRepository userExtendBindGrantRepository;

    /**
     * 分页查询用户扩展绑定授权.
     *
     * @param entity 查询条件
     * @return 分页查询结果
     */
    public IPage<UserExtendBindGrantPageVO> page(UserExtendBindGrantSearchDTO entity) {
        return this.userExtendBindGrantRepository.page(entity);
    }

    /**
     * 根据id查询用户扩展绑定授权详情.
     *
     * @param id id
     * @return 用户扩展绑定授权详情
     */
    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public UserExtendBindGrantVO getDetail(Long id) {
        return this.userExtendBindGrantRepository.getBaseMapper().selectDetailById(id);
    }
}
