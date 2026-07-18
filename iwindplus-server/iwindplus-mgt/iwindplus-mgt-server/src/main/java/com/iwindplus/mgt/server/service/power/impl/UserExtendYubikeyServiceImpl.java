package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeyEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeySaveDTO;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeySearchDTO;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import com.iwindplus.mgt.domain.vo.power.UserExtendYubikeyPageVO;
import com.iwindplus.mgt.domain.vo.power.UserExtendYubikeyVO;
import com.iwindplus.mgt.server.dal.model.power.UserExtendYubikeyDO;
import com.iwindplus.mgt.server.dal.repository.power.UserExtendYubikeyRepository;
import com.iwindplus.mgt.server.service.power.UserExtendYubikeyService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户扩展yubikey业务层实现类.
 *
 * @author zengdegui
 * @since 2026/04/26 20:26
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserExtendYubikeyServiceImpl implements UserExtendYubikeyService {

    private final UserExtendYubikeyRepository userYubikeyRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserExtendYubikeySaveDTO entity) {
        this.userYubikeyRepository.getYubikeyIsExist(entity.getUserId(), entity.getBizType());
        final UserExtendYubikeyDO model = BeanUtil.copyProperties(entity, UserExtendYubikeyDO.class);
        this.userYubikeyRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<UserExtendYubikeyDO> list = this.userYubikeyRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.userYubikeyRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY}, allEntries = true),
        }
    )
    @Override
    public boolean edit(UserExtendYubikeyEditDTO entity) {
        UserExtendYubikeyDO data = this.userYubikeyRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final UserExtendYubikeyDO model = BeanUtil.copyProperties(entity, UserExtendYubikeyDO.class);
        this.userYubikeyRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Override
    public IPage<UserExtendYubikeyPageVO> pageByUserId(UserExtendYubikeySearchDTO entity) {
        final PageDTO<UserExtendYubikeyDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<UserExtendYubikeyDO> queryWrapper = Wrappers.lambdaQuery(UserExtendYubikeyDO.class)
            .eq(UserExtendYubikeyDO::getUserId, entity.getUserId());
        return this.userYubikeyRepository.page(page, queryWrapper)
            .convert(model -> BeanUtil.copyProperties(model, UserExtendYubikeyPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public UserExtendYubikeyVO getDetail(Long id) {
        UserExtendYubikeyDO data = this.userYubikeyRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, UserExtendYubikeyVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    @Override
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
