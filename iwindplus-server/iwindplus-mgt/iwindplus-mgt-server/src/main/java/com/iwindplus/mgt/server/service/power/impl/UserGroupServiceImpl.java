/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserGroupSaveEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserGroupSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.power.UserGroupBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupPageVO;
import com.iwindplus.mgt.server.dal.model.power.UserGroupDO;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupRoleRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupUserRepository;
import com.iwindplus.mgt.server.service.power.UserGroupService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserGroupServiceImpl implements UserGroupService {

    private final RedissonService redissonService;
    private final UserGroupRepository userGroupRepository;
    private final UserGroupUserRepository userGroupUserRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserGroupSaveEditDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.userGroupRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.USER_GROUP_PREFIX.getValue()));
        }
        this.userGroupRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        entity.setSeq(this.userGroupRepository.getNextSeq(entity.getOrgId()));
        final UserGroupDO model = BeanUtil.copyProperties(entity, UserGroupDO.class);
        this.userGroupRepository.save(model);
        this.userGroupRoleRepository.saveBatchRole(model.getId(), entity.getRoleIds());
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<UserGroupDO> list = this.userGroupRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(UserGroupDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.userGroupUserRepository.getBaseMapper().deleteByUserGroupIds(ids);
        this.userGroupRoleRepository.getBaseMapper().deleteByUserGroupIds(ids);
        this.userGroupRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean edit(UserGroupSaveEditDTO entity) {
        // 编辑
        UserGroupDO data = this.userGroupRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.userGroupRepository.getNameIsExist(entity.getName().trim(), data.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.userGroupRepository.getCodeIsExist(entity.getCode().trim(), data.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final UserGroupDO model = BeanUtil.copyProperties(entity, UserGroupDO.class);
        this.userGroupRepository.updateById(model);
        this.userGroupRoleRepository.editBatchRole(model.getId(), entity.getRoleIds());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        UserGroupDO data = this.userGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        UserGroupDO param = new UserGroupDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.userGroupRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        UserGroupDO data = this.userGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        UserGroupDO param = new UserGroupDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.userGroupRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<UserGroupPageVO> page(UserGroupSearchDTO entity) {
        PageDTO<UserGroupDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.userGroupRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public UserGroupExtendVO getDetailExtend(Long id) {
        return this.userGroupRepository.getBaseMapper().selectDetailById(id);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1 + '_' + #p2", condition = "#p1 != null", unless = "#result == null")
    @Override
    public List<UserGroupBaseCheckedVO> listByUserId(Long orgId, Long userId) {
        List<UserGroupBaseCheckedVO> allList = this.userGroupRepository.getBaseMapper().selectListByOrgId(orgId);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(userId)) {
            List<UserGroupBaseCheckedVO> checkedList = this.userGroupRepository.getBaseMapper().selectListByUserId(orgId, userId);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<UserGroupBaseCheckedVO> listByRoleId(Long orgId, Long roleId) {
        List<UserGroupBaseCheckedVO> allList = this.userGroupRepository.getBaseMapper().selectListByOrgId(orgId);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(roleId)) {
            List<UserGroupBaseCheckedVO> checkedList = this.userGroupRepository.getBaseMapper().selectListByRoleId(orgId, roleId);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    private List<UserGroupBaseCheckedVO> listWithChecked(List<UserGroupBaseCheckedVO> allList, List<UserGroupBaseCheckedVO> checkedList) {
        return allList.stream().filter(Objects::nonNull).peek(map -> checkedList.stream()
                .filter(m -> Objects.equals(m.getId(), map.getId())).forEach(m -> map.setChecked(m.getChecked())))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
