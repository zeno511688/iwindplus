/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseTreeCheckedVO;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.DepartmentEditDTO;
import com.iwindplus.mgt.domain.dto.power.DepartmentSaveDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.power.DepartmentBaseVO;
import com.iwindplus.mgt.domain.vo.power.DepartmentExtendVO;
import com.iwindplus.mgt.server.dal.model.power.DepartmentDO;
import com.iwindplus.mgt.server.dal.model.power.PositionDO;
import com.iwindplus.mgt.server.dal.repository.power.DepartmentRepository;
import com.iwindplus.mgt.server.dal.repository.power.PositionRepository;
import com.iwindplus.mgt.server.service.power.DepartmentService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final RedissonService redissonService;
    private final PositionRepository positionRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true)
        }
    )
    @Override
    public boolean save(DepartmentSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.departmentRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId(), entity.getParentId());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.DEPARTMENT_PREFIX.getValue()));
        }
        this.departmentRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId(), entity.getParentId());
        entity.setSeq(this.departmentRepository.getNextSeq(entity.getOrgId(), entity.getParentId()));
        entity.setLevel(this.departmentRepository.getLevel(entity.getOrgId(), entity.getParentId()));
        final DepartmentDO model = BeanUtil.copyProperties(entity, DepartmentDO.class);
        this.departmentRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true)
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<DepartmentDO> list = this.departmentRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(DepartmentDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        // 判断是否有子集
        boolean data = SqlHelper.retBool(this.departmentRepository.count(Wrappers.lambdaQuery(DepartmentDO.class)
            .in(DepartmentDO::getParentId, ids)));
        if (data) {
            throw new BizException(MgtCodeEnum.CHILDREN_NOT_DELETED);
        }
        // 判断是否有职位
        final boolean hasPosition = SqlHelper.retBool(this.positionRepository.count(Wrappers.lambdaQuery(PositionDO.class)
            .in(PositionDO::getDepartmentId, ids)));
        if (Boolean.TRUE.equals(hasPosition)) {
            throw new BizException(MgtCodeEnum.POSITION_NOT_DELETED);
        }
        this.departmentRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true)
        }
    )
    @Override
    public boolean edit(DepartmentEditDTO entity) {
        DepartmentDO data = this.departmentRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        Long orgId = data.getOrgId();
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.departmentRepository.getNameIsExist(entity.getName().trim(), orgId, data.getParentId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.departmentRepository.getCodeIsExist(entity.getCode().trim(), orgId, data.getParentId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        entity.setLevel(this.departmentRepository.getLevel(orgId, entity.getParentId()));
        final DepartmentDO model = BeanUtil.copyProperties(entity, DepartmentDO.class);
        this.departmentRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true)
        }
    )
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        DepartmentDO data = this.departmentRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        DepartmentDO param = new DepartmentDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.departmentRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true)
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        DepartmentDO data = this.departmentRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        DepartmentDO param = new DepartmentDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.departmentRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<Tree<Long>> listByOrgId(Long orgId) {
        List<BaseTreeCheckedVO> allList = this.departmentRepository.getBaseMapper().selectListByOrgId(orgId, null);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        return this.listBaseTreeChecked(allList, 0L);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<Tree<Long>> listEnabledByOrgId(Long orgId) {
        List<BaseTreeCheckedVO> allList = this.departmentRepository.getBaseMapper().selectListByOrgId(orgId, EnableStatusEnum.ENABLE);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        return this.listBaseTreeChecked(allList, 0L);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public DepartmentExtendVO getDetailExtend(Long id) {
        return this.departmentRepository.getBaseMapper().selectDetailById(id);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    @Override
    public List<DepartmentBaseVO> listCheckedByUserId(Long orgId, Long userId) {
        return this.departmentRepository.getBaseMapper().selectListCheckedByUserId(orgId, userId);
    }

    private List<Tree<Long>> listBaseTreeChecked(List<BaseTreeCheckedVO> allList, Long rootId) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("seq");
        return TreeUtil.build(allList, rootId, config, (object, tree) -> {
            tree.setId(object.getId());
            tree.setParentId(object.getParentId());
            tree.setWeight(object.getSeq());
            tree.setName(object.getName());
            tree.putExtra("level", object.getLevel());
            Optional.ofNullable(object.getChecked()).filter(Objects::nonNull)
                .ifPresent(val -> tree.putExtra("checked", val));
        });
    }
}
