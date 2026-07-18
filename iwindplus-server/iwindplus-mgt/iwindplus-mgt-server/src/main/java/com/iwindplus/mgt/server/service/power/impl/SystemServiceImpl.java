/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.SystemEditDTO;
import com.iwindplus.mgt.domain.dto.power.SystemSaveDTO;
import com.iwindplus.mgt.domain.dto.power.SystemSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.power.SystemBaseVO;
import com.iwindplus.mgt.domain.vo.power.SystemExtendVO;
import com.iwindplus.mgt.domain.vo.power.SystemPageVO;
import com.iwindplus.mgt.domain.vo.power.SystemVO;
import com.iwindplus.mgt.server.config.property.MgtProperty;
import com.iwindplus.mgt.server.dal.model.power.SystemDO;
import com.iwindplus.mgt.server.dal.repository.power.SystemRepository;
import com.iwindplus.mgt.server.service.power.SystemService;
import com.iwindplus.setup.client.OssClient;
import java.util.ArrayList;
import java.util.Arrays;
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
 * 系统业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SYSTEM})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final SystemRepository systemRepository;
    private final OssClient ossClient;
    private final RedissonService redissonService;
    private final MgtProperty property;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    @Override
    public boolean save(SystemSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setHideFlag(Boolean.FALSE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.systemRepository.getNameIsExist(entity.getName().trim());
        entity.setSeq(this.systemRepository.getNextSeq());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.SYSTEM_PREFIX.getValue()));
        }
        this.systemRepository.getCodeIsExist(entity.getCode().trim());
        final SystemDO model = BeanUtil.copyProperties(entity, SystemDO.class);
        this.systemRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<SystemDO> list = this.systemRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().anyMatch(SystemDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.systemRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    @Override
    public boolean edit(SystemEditDTO entity) {
        SystemDO data = this.systemRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            // 校验名称是否存在
            this.systemRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.systemRepository.getCodeIsExist(entity.getCode().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final SystemDO model = BeanUtil.copyProperties(entity, SystemDO.class);
        this.systemRepository.updateById(model);
        this.removeOldPic(entity, data);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        SystemDO data = this.systemRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        SystemDO param = new SystemDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.systemRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        SystemDO data = this.systemRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        SystemDO param = new SystemDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.systemRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<SystemPageVO> page(SystemSearchDTO entity) {
        PageDTO<SystemDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(SystemDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(SystemDO::getName, entity.getName().trim());
        }
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(CommonConstant.DbConstant.MODIFIED_TIME);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(SystemDO::getId, SystemDO::getCreatedTime, SystemDO::getCreatedTimestamp, SystemDO::getCreatedBy,
            SystemDO::getModifiedTime, SystemDO::getModifiedTimestamp, SystemDO::getModifiedBy,
            SystemDO::getVersion, SystemDO::getStatus, SystemDO::getName, SystemDO::getHideFlag, SystemDO::getBuildInFlag
        );
        final PageDTO<SystemDO> modelPage = this.systemRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, SystemPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<SystemBaseVO> listByEnabled() {
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .eq(SystemDO::getStatus, EnableStatusEnum.ENABLE)
            .eq(SystemDO::getHideFlag, Boolean.FALSE)
            .select(SystemDO::getId, SystemDO::getCode, SystemDO::getName)
            .orderByAsc(Arrays.asList(SystemDO::getSeq));
        final List<SystemDO> list = this.systemRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, SystemBaseVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public SystemVO getDetail(Long id) {
        SystemDO data = this.systemRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, SystemVO.class);
    }

    @Override
    public SystemExtendVO getDetailExtend(Long id) {
        final SystemVO data = this.getDetail(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        SystemExtendVO result = BeanUtil.copyProperties(data, SystemExtendVO.class);
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(data.getIconUrl())) {
            relativePaths.add(data.getIconUrl());
        }
        List<FilePathVO> filePaths = OrgServiceImpl.getFilePaths(this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        if (CollUtil.isNotEmpty(filePaths)) {
            filePaths.forEach(p -> {
                if (CharSequenceUtil.isNotBlank(data.getIconUrl()) && data.getIconUrl().equals(p.getRelativePath())) {
                    result.setIconUrlStr(p.getAbsolutePath());
                }
            });
        }
        return result;
    }

    private void removeOldPic(SystemEditDTO entity, SystemDO data) {
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(entity.getIconUrl())
            && CharSequenceUtil.isNotBlank(data.getIconUrl())
            && !CharSequenceUtil.equals(data.getIconUrl(), entity.getIconUrl().trim())) {
            relativePaths.add(data.getIconUrl());
        }
        OrgServiceImpl.removeFiles(this.ossClient, this.property.getOss().getTplCode(), relativePaths);
    }

}
