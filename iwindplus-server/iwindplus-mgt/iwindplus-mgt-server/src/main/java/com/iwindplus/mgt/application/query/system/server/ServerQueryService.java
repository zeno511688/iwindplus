/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.api.system.vo.ServerRouteDefinitionVO;
import com.iwindplus.mgt.application.query.system.server.dto.ServerSearchDTO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerBaseVO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerPageVO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.server.ServerDO;
import com.iwindplus.mgt.infrastructure.persistence.system.server.ServerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 服务查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SERVER})
@RequiredArgsConstructor
public class ServerQueryService {

    private final ServerRepository serverRepository;

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ServerPageVO> page(ServerSearchDTO entity) {
        return this.serverRepository.page(entity);
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<ServerRouteDefinitionVO> listRouteDefinition() {
        LambdaQueryWrapper<ServerDO> queryWrapper = Wrappers.lambdaQuery(ServerDO.class)
            .eq(ServerDO::getStatus, EnableStatusEnum.ENABLE)
            .orderByAsc(List.of(ServerDO::getSeq, ServerDO::getRouteId));
        List<ServerDO> list = this.serverRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        final List<ServerRouteDefinitionVO> data = new ArrayList<>(10);
        list.forEach(item -> {
            final ServerRouteDefinitionVO build = ServerRouteDefinitionVO.builder()
                .id(item.getRouteId())
                .uri(item.getUri())
                .predicates(item.getPredicates())
                .filters(item.getFilters())
                .metadata(item.getMetadata())
                .order(item.getSeq())
                .build();
            data.add(build);
        });
        return data;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<ServerBaseVO> listEnabled() {
        LambdaQueryWrapper<ServerDO> queryWrapper = Wrappers.lambdaQuery(ServerDO.class)
            .eq(ServerDO::getStatus, EnableStatusEnum.ENABLE)
            .eq(ServerDO::getHideFlag, Boolean.FALSE)
            .select(ServerDO::getId, ServerDO::getRouteId, ServerDO::getName)
            .orderByAsc(List.of(ServerDO::getSeq));
        List<ServerDO> list = this.serverRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, ServerBaseVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public ServerVO getDetail(Long id) {
        ServerDO data = this.serverRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ServerVO.class);
    }

    public Long getIdByRouteId(String routeId) {
        return this.serverRepository.getIdByRouteId(routeId);
    }
}
