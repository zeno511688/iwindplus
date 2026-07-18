/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.log.client.SmsCaptchaLogClient;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserSaveByThirdDTO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSaveEditDTO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSearchDTO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantUserDTO;
import com.iwindplus.mgt.domain.vo.power.UserBindResultVO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantVO;
import com.iwindplus.mgt.server.config.property.MgtProperty;
import com.iwindplus.mgt.server.dal.model.system.ThirdBindGrantDO;
import com.iwindplus.mgt.server.dal.repository.system.ThirdBindGrantRepository;
import com.iwindplus.mgt.server.service.power.UserService;
import com.iwindplus.mgt.server.service.system.ThirdBindGrantService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 第三方绑定授权业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_THIRD_BIND_GRANT})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ThirdBindGrantServiceImpl implements ThirdBindGrantService {

    private final UserService userService;
    private final ThirdBindGrantRepository thirdBindGrantRepository;
    private final SmsCaptchaLogClient smsCaptchaLogClient;
    private final MgtProperty property;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_THIRD_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    @Override
    public ThirdBindGrantResultVO saveOrEdit(ThirdBindGrantSaveEditDTO entity) {
        boolean bindFlag = false;
        String code = IdUtil.simpleUUID();
        final LambdaQueryWrapper<ThirdBindGrantDO> queryWrapper = Wrappers.lambdaQuery(ThirdBindGrantDO.class)
            .eq(ThirdBindGrantDO::getOpenid, entity.getOpenid().trim())
            .eq(ThirdBindGrantDO::getType, entity.getType());
        if (CharSequenceUtil.isNotBlank(entity.getUnionId())) {
            queryWrapper.eq(ThirdBindGrantDO::getUnionId, entity.getUnionId().trim());
        }
        ThirdBindGrantVO data = this.thirdBindGrantRepository.getBaseMapper().selectByOpenId(entity.getOpenid(), entity.getUnionId(), entity.getType());
        if (Objects.isNull(data)) {
            if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
                bindFlag = this.getBindFlag(entity);
            }
            entity.setCode(code);
            entity.setRemark(entity.getType().getDesc());
            ThirdBindGrantDO model = BeanUtil.copyProperties(entity, ThirdBindGrantDO.class);
            this.thirdBindGrantRepository.save(model);
        } else {
            ThirdBindGrantDO param = new ThirdBindGrantDO();
            param.setId(data.getId());
            param.setCode(code);
            param.setVersion(data.getVersion());
            this.thirdBindGrantRepository.updateById(param);
            bindFlag = Objects.nonNull(data.getUserId()) && Objects.nonNull(data.getMobile());
        }
        return ThirdBindGrantResultVO.builder()
            .code(code)
            .bindFlag(bindFlag)
            .build();
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_THIRD_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        return CollUtil.isNotEmpty(ids) && SqlHelper.retBool(this.thirdBindGrantRepository.getBaseMapper().deleteByIds(ids));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_THIRD_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    @Override
    public boolean removeByUserIds(List<Long> userIds) {
        return CollUtil.isNotEmpty(userIds) && SqlHelper.retBool(this.thirdBindGrantRepository.getBaseMapper().deleteByUserIds(userIds));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_THIRD_BIND_GRANT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    @Override
    public boolean editUser(ThirdBindGrantUserDTO entity) {
        this.smsCaptchaLogClient.validate(this.property.getSms().getTplCode(), entity.getMobile(), entity.getCaptcha());
        ThirdBindGrantDO data = this.thirdBindGrantRepository.getOne(Wrappers.lambdaQuery(ThirdBindGrantDO.class)
            .eq(ThirdBindGrantDO::getCode, entity.getCode().trim()));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.CODE_NOT_EXIST);
        }
        Long userId = this.getUserId(entity);
        ThirdBindGrantDO param = new ThirdBindGrantDO();
        param.setId(data.getId());
        param.setUserId(userId);
        param.setVersion(data.getVersion());
        this.thirdBindGrantRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<ThirdBindGrantVO> page(ThirdBindGrantSearchDTO entity) {
        PageDTO<ThirdBindGrantDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.thirdBindGrantRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public ThirdBindGrantVO getDetail(Long id) {
        return this.thirdBindGrantRepository.getBaseMapper().selectDetailById(id);
    }

    private boolean getBindFlag(ThirdBindGrantSaveEditDTO entity) {
        UserSaveByThirdDTO condition = new UserSaveByThirdDTO();
        condition.setMobile(entity.getMobile());
        final UserBindResultVO userBindResult = this.userService.editBindByMobile(condition);
        if (Objects.nonNull(userBindResult)) {
            entity.setUserId(userBindResult.getUserId());
            return Objects.nonNull(userBindResult.getUserId()) && userBindResult.getBindMobileFlag();
        }
        return Boolean.FALSE;
    }

    private Long getUserId(ThirdBindGrantUserDTO entity) {
        UserSaveByThirdDTO condition = new UserSaveByThirdDTO();
        condition.setMobile(entity.getMobile());
        condition.setNickName(entity.getNickName());
        condition.setSex(entity.getSex());
        condition.setLocationCountry(entity.getCountry());
        condition.setLocationProvince(entity.getProvince());
        condition.setLocationCity(entity.getCity());
        condition.setAvatar(entity.getAvatar());
        final UserBindResultVO userBindResult = this.userService.editBindByMobile(condition);
        return Optional.ofNullable(userBindResult.getUserId()).orElse(null);
    }
}
