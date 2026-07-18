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
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.PositionEditDTO;
import com.iwindplus.mgt.domain.dto.power.PositionSaveDTO;
import com.iwindplus.mgt.domain.dto.power.PositionSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.power.PositionBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.PositionExtendVO;
import com.iwindplus.mgt.domain.vo.power.PositionPageVO;
import com.iwindplus.mgt.server.dal.model.power.PositionDO;
import com.iwindplus.mgt.server.dal.repository.power.PositionRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserPositionRepository;
import com.iwindplus.mgt.server.service.power.PositionService;
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
 * 职位业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_POSITION})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final RedissonService redissonService;
    private final PositionRepository positionRepository;
    private final UserPositionRepository userPositionRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean save(PositionSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.positionRepository.getNameIsExist(entity.getName().trim(), entity.getDepartmentId(), entity.getOrgId());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.POSITION_PREFIX.getValue()));
        }
        this.positionRepository.getCodeIsExist(entity.getCode().trim(), entity.getDepartmentId(), entity.getOrgId());
        entity.setSeq(this.positionRepository.getNextSeq(entity.getDepartmentId(), entity.getOrgId()));
        final PositionDO model = BeanUtil.copyProperties(entity, PositionDO.class);
        this.positionRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<PositionDO> list = this.positionRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(PositionDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.userPositionRepository.getBaseMapper().deleteByPositionIds(ids);
        this.positionRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean edit(PositionEditDTO entity) {
        PositionDO data = this.positionRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.positionRepository.getNameIsExist(entity.getName().trim(), data.getDepartmentId(), data.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.positionRepository.getCodeIsExist(entity.getCode().trim(), data.getDepartmentId(), data.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final PositionDO model = BeanUtil.copyProperties(entity, PositionDO.class);
        this.positionRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        PositionDO data = this.positionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        PositionDO param = new PositionDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.positionRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        PositionDO data = this.positionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        PositionDO param = new PositionDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.positionRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<PositionPageVO> page(PositionSearchDTO entity) {
        PageDTO<PositionDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<PositionDO> queryWrapper = Wrappers.lambdaQuery(PositionDO.class)
            .eq(PositionDO::getDepartmentId, entity.getDepartmentId());
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(PositionDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(PositionDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(PositionDO::getName, entity.getName().trim());
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
        queryWrapper.select(PositionDO::getId, PositionDO::getCreatedTime, PositionDO::getCreatedTimestamp, PositionDO::getCreatedBy,
            PositionDO::getModifiedTime, PositionDO::getModifiedTimestamp, PositionDO::getModifiedBy, PositionDO::getVersion, PositionDO::getStatus,
            PositionDO::getCode, PositionDO::getName, PositionDO::getBuildInFlag, PositionDO::getDepartmentId
        );
        final PageDTO<PositionDO> modelPage = this.positionRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, PositionPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public PositionExtendVO getDetailExtend(Long id) {
        return this.positionRepository.getBaseMapper().selectDetailById(id);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p1 != null", unless = "#result == null")
    @Override
    public List<PositionBaseCheckedVO> listByUserId(Long orgId, Long userId, List<Long> departmentIds) {
        List<PositionBaseCheckedVO> allList = this.positionRepository.getBaseMapper().selectListByOrgId(orgId, departmentIds);
        if (CollUtil.isEmpty(allList)) {
            return null;
        }
        if (Objects.nonNull(userId)) {
            List<PositionBaseCheckedVO> checkedList = this.positionRepository.getBaseMapper().selectListByUserId(orgId, userId, departmentIds);
            if (CollUtil.isNotEmpty(checkedList)) {
                allList = this.listWithChecked(allList, checkedList);
            }
        }
        return allList;
    }

    private List<PositionBaseCheckedVO> listWithChecked(List<PositionBaseCheckedVO> allList, List<PositionBaseCheckedVO> checkedList) {
        return allList.stream().filter(Objects::nonNull).peek(map -> checkedList.stream()
                .filter(m -> Objects.equals(m.getId(), map.getId())).forEach(m -> map.setChecked(m.getChecked())))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
