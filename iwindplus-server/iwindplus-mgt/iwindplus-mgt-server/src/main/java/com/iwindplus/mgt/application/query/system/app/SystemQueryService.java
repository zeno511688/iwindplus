/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.application.query.system.app.vo.SystemBaseVO;
import com.iwindplus.mgt.application.query.system.app.vo.SystemExtendVO;
import com.iwindplus.mgt.application.query.system.app.vo.SystemPageVO;
import com.iwindplus.mgt.application.query.system.app.vo.SystemVO;
import com.iwindplus.mgt.application.service.system.app.dto.SystemSearchDTO;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.system.app.SystemDO;
import com.iwindplus.mgt.infrastructure.persistence.system.app.SystemRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 系统查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SYSTEM})
@RequiredArgsConstructor
public class SystemQueryService {

    private final SystemRepository systemRepository;
    private final OssClient ossClient;
    private final MgtProperty property;

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
            OrderItem item = OrderItem.desc(DbConstant.MODIFIED_TIMESTAMP);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(SystemDO::getId, SystemDO::getCreatedTimestamp, SystemDO::getCreatedBy,
            SystemDO::getModifiedTimestamp, SystemDO::getModifiedBy,
            SystemDO::getVersion, SystemDO::getStatus, SystemDO::getName, SystemDO::getHideFlag, SystemDO::getBuildInFlag
        );
        final PageDTO<SystemDO> modelPage = this.systemRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, SystemPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<SystemBaseVO> listByEnabled() {
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .eq(SystemDO::getStatus, EnableStatusEnum.ENABLE)
            .eq(SystemDO::getHideFlag, Boolean.FALSE)
            .select(SystemDO::getId, SystemDO::getCode, SystemDO::getName)
            .orderByAsc(List.of(SystemDO::getSeq));
        final List<SystemDO> list = this.systemRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, SystemBaseVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public SystemVO getDetail(Long id) {
        SystemDO data = this.systemRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, SystemVO.class);
    }

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
        List<FilePathVO> filePaths = OrgApplicationService.getFilePaths(
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        if (CollUtil.isNotEmpty(filePaths)) {
            filePaths.forEach(p -> {
                if (CharSequenceUtil.isNotBlank(data.getIconUrl()) && data.getIconUrl().equals(p.getRelativePath())) {
                    result.setIconUrlStr(p.getAbsolutePath());
                }
            });
        }
        return result;
    }
}
