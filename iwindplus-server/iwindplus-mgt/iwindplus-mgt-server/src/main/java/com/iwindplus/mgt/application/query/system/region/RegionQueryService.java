/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.region;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.system.region.vo.RegionBaseTreeVO;
import com.iwindplus.mgt.application.query.system.region.vo.RegionVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.region.RegionDO;
import com.iwindplus.mgt.infrastructure.persistence.system.region.RegionRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 省市区查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_REGION})
@RequiredArgsConstructor
public class RegionQueryService {

    private final RegionRepository regionRepository;

    @Cacheable(key = "#root.methodName + '_' + #p0", unless = "#result == null")
    public List<Tree<Long>> listByEnabled(EnableStatusEnum status) {
        final LambdaQueryWrapper<RegionDO> queryWrapper = Wrappers.lambdaQuery(RegionDO.class)
            .select(RegionDO::getId, RegionDO::getName, RegionDO::getCode, RegionDO::getLevel, RegionDO::getSeq, RegionDO::getParentId)
            .orderByAsc(List.of(RegionDO::getSeq));
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
