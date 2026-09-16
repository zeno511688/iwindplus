/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.organization;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.mgt.application.query.upms.organization.dto.PositionSearchDTO;
import com.iwindplus.mgt.application.query.upms.organization.vo.PositionBaseCheckedVO;
import com.iwindplus.mgt.application.query.upms.organization.vo.PositionExtendVO;
import com.iwindplus.mgt.application.query.upms.organization.vo.PositionPageVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.PositionDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.PositionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 职位查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_POSITION})
@RequiredArgsConstructor
public class PositionQueryService {

    private final PositionRepository positionRepository;

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
            OrderItem item = OrderItem.desc(DbConstant.MODIFIED_TIMESTAMP);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(PositionDO::getId, PositionDO::getCreatedTimestamp, PositionDO::getCreatedBy,
            PositionDO::getModifiedTimestamp, PositionDO::getModifiedBy, PositionDO::getVersion, PositionDO::getStatus,
            PositionDO::getCode, PositionDO::getName, PositionDO::getBuildInFlag, PositionDO::getDepartmentId
        );
        final PageDTO<PositionDO> modelPage = this.positionRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, PositionPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public PositionExtendVO getDetailExtend(Long id) {
        return this.positionRepository.getBaseMapper().selectDetailById(id);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p1 != null", unless = "#result == null")
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
