/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantVO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendBindGrantSaveEditDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendBindGrantUserDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserSaveByBindGrantDTO;
import com.iwindplus.mgt.application.service.upms.user.vo.UserBindResultVO;
import com.iwindplus.mgt.application.service.upms.user.vo.UserExtendBindGrantResultVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendBindGrantDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendBindGrantRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户扩展绑定授权业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_BIND_GRANT})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserExtendBindGrantApplicationService {

    private final UserApplicationService userApplicationService;
    private final UserExtendBindGrantRepository userExtendBindGrantRepository;
    private final SmsCaptchaLogClient smsCaptchaLogClient;
    private final MgtProperty property;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public UserExtendBindGrantResultVO saveOrEdit(UserExtendBindGrantSaveEditDTO entity) {
        boolean bindFlag = false;
        String code = IdUtil.simpleUUID();
        final LambdaQueryWrapper<UserExtendBindGrantDO> queryWrapper = Wrappers.lambdaQuery(UserExtendBindGrantDO.class)
            .eq(UserExtendBindGrantDO::getOpenid, entity.getOpenid().trim())
            .eq(UserExtendBindGrantDO::getType, entity.getType());
        if (CharSequenceUtil.isNotBlank(entity.getUnionId())) {
            queryWrapper.eq(UserExtendBindGrantDO::getUnionId, entity.getUnionId().trim());
        }
        UserExtendBindGrantVO data = this.userExtendBindGrantRepository.getBaseMapper().selectByOpenId(entity.getOpenid(), entity.getUnionId(), entity.getType());
        if (Objects.isNull(data)) {
            if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
                bindFlag = this.getBindFlag(entity);
            }
            entity.setCode(code);
            entity.setRemark(entity.getType().getDesc());
            UserExtendBindGrantDO model = BeanUtil.copyProperties(entity, UserExtendBindGrantDO.class);
            this.userExtendBindGrantRepository.save(model);
        } else {
            UserExtendBindGrantDO param = new UserExtendBindGrantDO();
            param.setId(data.getId());
            param.setCode(code);
            param.setVersion(data.getVersion());
            this.userExtendBindGrantRepository.updateById(param);
            bindFlag = Objects.nonNull(data.getUserId()) && Objects.nonNull(data.getMobile());
        }
        return UserExtendBindGrantResultVO.builder()
            .code(code)
            .bindFlag(bindFlag)
            .build();
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean removeByIds(List<Long> ids) {
        return CollUtil.isNotEmpty(ids) && SqlHelper.retBool(this.userExtendBindGrantRepository.getBaseMapper().deleteByIds(ids));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean removeByUserIds(List<Long> userIds) {
        return CollUtil.isNotEmpty(userIds) && SqlHelper.retBool(this.userExtendBindGrantRepository.getBaseMapper().deleteByUserIds(userIds));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean editUser(UserExtendBindGrantUserDTO entity) {
        this.smsCaptchaLogClient.validate(this.property.getSms().getTplCode(), entity.getMobile(), entity.getCaptcha());
        UserExtendBindGrantDO data = this.userExtendBindGrantRepository.getOne(Wrappers.lambdaQuery(UserExtendBindGrantDO.class)
            .eq(UserExtendBindGrantDO::getCode, entity.getCode().trim()));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.CODE_NOT_EXIST);
        }
        Long userId = this.getUserId(entity);
        UserExtendBindGrantDO param = new UserExtendBindGrantDO();
        param.setId(data.getId());
        param.setUserId(userId);
        param.setVersion(data.getVersion());
        this.userExtendBindGrantRepository.updateById(param);
        return Boolean.TRUE;
    }

    private boolean getBindFlag(UserExtendBindGrantSaveEditDTO entity) {
        final UserSaveByBindGrantDTO condition = UserSaveByBindGrantDTO
            .builder()
            .mobile(entity.getMobile())
            .build();
        final UserBindResultVO userBindResult = this.userApplicationService.editBindByMobile(condition);
        if (Objects.nonNull(userBindResult)) {
            entity.setUserId(userBindResult.getUserId());
            return Objects.nonNull(userBindResult.getUserId()) && userBindResult.getBindMobileFlag();
        }
        return Boolean.FALSE;
    }

    private Long getUserId(UserExtendBindGrantUserDTO entity) {
        final UserSaveByBindGrantDTO condition = UserSaveByBindGrantDTO
            .builder()
            .mobile(entity.getMobile())
            .nickName(entity.getNickName())
            .sex(entity.getSex())
            .locationCountry(entity.getCountry())
            .locationProvince(entity.getProvince())
            .locationCity(entity.getCity())
            .avatar(entity.getAvatar())
            .build();
        final UserBindResultVO userBindResult = this.userApplicationService.editBindByMobile(condition);
        return Optional.ofNullable(userBindResult).map(UserBindResultVO::getUserId).orElse(null);
    }
}
