/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.RegionEditDTO;
import com.iwindplus.setup.domain.dto.RegionSaveDTO;
import com.iwindplus.setup.domain.vo.RegionBaseTreeVO;
import com.iwindplus.setup.domain.vo.RegionVO;
import com.iwindplus.setup.server.dal.model.RegionDO;
import com.iwindplus.setup.server.dal.repository.RegionRepository;
import com.iwindplus.setup.server.service.RegionService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 省市区业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_REGION})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(RegionSaveDTO entity) {
        this.regionRepository.getNameIsExist(entity.getName().trim(), entity.getParentId());
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        entity.setSeq(this.regionRepository.getNextSeq(entity.getParentId()));
        entity.setLevel(this.regionRepository.getLevel(entity.getParentId()));
        final RegionDO model = BeanUtil.copyProperties(entity, RegionDO.class);
        this.regionRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<RegionDO> list = this.regionRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(RegionDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        // 判断是否有子集
        boolean data = SqlHelper.retBool(this.regionRepository.count(Wrappers.lambdaQuery(RegionDO.class)
            .in(RegionDO::getParentId, ids)));
        if (data) {
            throw new BizException(MgtCodeEnum.CHILDREN_NOT_DELETED);
        }
        this.regionRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(RegionEditDTO entity) {
        RegionDO data = this.regionRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.regionRepository.getNameIsExist(entity.getName().trim(), entity.getParentId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        entity.setLevel(this.regionRepository.getLevel(entity.getParentId()));
        final RegionDO model = BeanUtil.copyProperties(entity, RegionDO.class);
        this.regionRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        RegionDO data = this.regionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        RegionDO entity = new RegionDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.regionRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        RegionDO data = this.regionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        RegionDO param = new RegionDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.regionRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", unless = "#result == null")
    @Override
    public List<Tree<Long>> listByEnabled(EnableStatusEnum status) {
        final LambdaQueryWrapper<RegionDO> queryWrapper = Wrappers.lambdaQuery(RegionDO.class)
            .select(RegionDO::getId, RegionDO::getName, RegionDO::getCode, RegionDO::getLevel, RegionDO::getSeq, RegionDO::getParentId)
            .orderByAsc(Arrays.asList(RegionDO::getSeq));
        if (Objects.nonNull(status)) {
            queryWrapper.eq(RegionDO::getStatus, status);
        }
        List<RegionDO> list = this.regionRepository.list(queryWrapper);
        List<RegionBaseTreeVO> allList = BeanUtil.copyToList(list, RegionBaseTreeVO.class);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        return this.listTree(allList, 0L);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public RegionVO getDetail(Long id) {
        RegionDO data = this.regionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, RegionVO.class);
    }

    private List<Tree<Long>> listTree(List<RegionBaseTreeVO> allList, Long rootId) {
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("seq");
        return TreeUtil.build(allList, rootId, config, (object, tree) -> {
            tree.setId(object.getId());
            tree.setParentId(object.getParentId());
            tree.setWeight(object.getSeq());
            tree.setName(object.getName());
            tree.putExtra("code", object.getCode());
            tree.putExtra("level", object.getLevel());
        });
    }

}
