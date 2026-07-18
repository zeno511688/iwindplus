/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.GatewayRouteConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.ServerDTO;
import com.iwindplus.mgt.domain.dto.system.ServerRouteParamDTO;
import com.iwindplus.mgt.domain.dto.system.ServerSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ServerBaseVO;
import com.iwindplus.mgt.domain.vo.system.ServerPageVO;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
import com.iwindplus.mgt.domain.vo.system.ServerVO;
import com.iwindplus.mgt.server.dal.model.system.ServerDO;
import com.iwindplus.mgt.server.dal.repository.system.ServerRepository;
import com.iwindplus.mgt.server.service.system.ServerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 服务业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SERVER})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final Optional<NacosConfigManager> nacosConfigManagerOpt;
    private final ServerRepository serverRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true)
        }
    )
    @Override
    public boolean save(ServerDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setHideFlag(Boolean.FALSE);
        this.serverRepository.getNameIsExist(entity.getName().trim());
        // 校验路由ID是否存在
        this.serverRepository.getRouteIdIsExist(entity.getRouteId().trim());
        // 判断路由规则中路由地址是否重复配置
        this.serverRepository.getPredicatesIsExist(entity.getPredicates());
        entity.setSeq(this.serverRepository.getNextSeq());
        entity.setBuildInFlag(Boolean.FALSE);
        ServerDO model = BeanUtil.copyProperties(entity, ServerDO.class);
        boolean data = this.serverRepository.save(model);
        if (data) {
            entity.setId(model.getId());
            sendMsgAsyncWithRetry().subscribe();
        }
        return data;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true)
        }
    )
    @Override
    public boolean saveOrEditBatch(List<ServerDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return false;
        }
        final Integer nextSeq = this.serverRepository.getNextSeq();
        AtomicInteger seq = new AtomicInteger(nextSeq);
        List<ServerDTO> saveList = new ArrayList<>(10);
        List<ServerDTO> editList = new ArrayList<>(10);
        entities.forEach(entity -> {
            final ServerDO data = this.serverRepository.getOne(Wrappers.lambdaQuery(ServerDO.class)
                .eq(ServerDO::getRouteId, entity.getRouteId())
                .select(ServerDO::getId, ServerDO::getVersion));
            // 为空则添加
            if (Objects.isNull(data)) {
                if (Boolean.FALSE.equals(this.serverRepository.checkPredicates(entity.getPredicates()))) {
                    entity.setSeq(seq.incrementAndGet());
                    saveList.add(entity);
                }
            } else {
                entity.setId(data.getId());
                entity.setVersion(data.getVersion());
                editList.add(entity);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            List<ServerDO> doList = BeanUtil.copyToList(saveList, ServerDO.class);
            this.serverRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<ServerDO> doList = BeanUtil.copyToList(editList, ServerDO.class);
            this.serverRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);
        }
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true)
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<ServerDO> list = this.serverRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(ServerDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        final boolean result = this.serverRepository.removeByIds(ids);
        if (result) {
            sendMsgAsyncWithRetry().subscribe();
        }
        return result;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true)
        }
    )
    @Override
    public boolean edit(ServerDTO entity) {
        ServerDO data = this.serverRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.serverRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRouteId()) && !CharSequenceUtil.equals(data.getRouteId(), entity.getRouteId().trim())) {
            this.serverRepository.getRouteIdIsExist(entity.getRouteId().trim());
        }
        List<ServerRouteParamDTO> dtoList = BeanUtil.copyToList(data.getPredicates(), ServerRouteParamDTO.class);
        if (CollUtil.isNotEmpty(entity.getPredicates())
            && !CharSequenceUtil.equals(this.serverRepository.getPattern(dtoList), this.serverRepository.getPattern(entity.getPredicates()))) {
            this.serverRepository.getPredicatesIsExist(entity.getPredicates());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        ServerDO model = BeanUtil.copyProperties(entity, ServerDO.class);
        boolean result = this.serverRepository.updateById(model);
        if (result) {
            sendMsgAsyncWithRetry().subscribe();
        }
        return result;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true)
        }
    )
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        ServerDO data = this.serverRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ServerDO entity = new ServerDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        boolean result = this.serverRepository.updateById(entity);
        if (result) {
            sendMsgAsyncWithRetry().subscribe();
        }
        return result;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true)
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        ServerDO data = this.serverRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ServerDO param = new ServerDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.serverRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<ServerPageVO> page(ServerSearchDTO entity) {
        PageDTO<ServerDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ServerDO> queryWrapper = Wrappers.lambdaQuery(ServerDO.class)
            .orderByDesc(ServerDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ServerDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(ServerDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getRouteId())) {
            queryWrapper.eq(ServerDO::getRouteId, entity.getRouteId().trim());
        }
        queryWrapper.select(ServerDO::getId, ServerDO::getCreatedTime, ServerDO::getCreatedTimestamp, ServerDO::getCreatedBy,
            ServerDO::getModifiedTime, ServerDO::getModifiedTimestamp, ServerDO::getModifiedBy,
            ServerDO::getVersion, ServerDO::getStatus, ServerDO::getName, ServerDO::getRouteId, ServerDO::getUri, ServerDO::getHideFlag,
            ServerDO::getBuildInFlag
        );
        final PageDTO<ServerDO> modelPage = this.serverRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ServerPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<ServerRouteDefinitionVO> listRouteDefinition() {
        LambdaQueryWrapper<ServerDO> queryWrapper = Wrappers.lambdaQuery(ServerDO.class)
            .eq(ServerDO::getStatus, EnableStatusEnum.ENABLE)
            .orderByAsc(List.of(ServerDO::getSeq, ServerDO::getRouteId));
        List<ServerDO> list = this.serverRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        final List<ServerRouteDefinitionVO> data = new ArrayList<>(10);
        list.stream().forEach(item -> {
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
    @Override
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
    @Override
    public ServerVO getDetail(Long id) {
        ServerDO data = this.serverRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ServerVO.class);
    }

    @Override
    public Long getIdByRouteId(String routeId) {
        return this.serverRepository.getIdByRouteId(routeId);
    }

    @Override
    public void flush() {
        sendMsgAsyncWithRetry().subscribe();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        sendMsgAsyncWithRetry().subscribe();
    }

    private Mono<Void> sendMsgAsyncWithRetry() {
        return Mono.fromSupplier(this::listRouteDefinition)
            .subscribeOn(Schedulers.boundedElastic())
            .filter(ObjectUtil::isNotEmpty)
            .flatMap(list -> Mono.fromRunnable(() ->
                pushToNacos(list)
            ))
            .then()
            .doOnSuccess(v -> log.info("Server routing data pushed successfully"))
            .doOnError(error -> log.error("Error in server routing data push flow", error));
    }

    private boolean pushToNacos(List<ServerRouteDefinitionVO> list) {
        if (nacosConfigManagerOpt.isEmpty()) {
            log.warn("NacosConfigManager not present, skip route push");
            return false;
        }
        NacosConfigManager nacosConfigManager = nacosConfigManagerOpt.get();

        String dataId = GatewayRouteConstant.GATEWAY_ROUTE_FILE_NAME;
        String group = GatewayRouteConstant.GATEWAY_GROUP;

        try {

            String content = JacksonUtil.toJsonPrettyStr(list);
            boolean ok = nacosConfigManager.getConfigService().publishConfig(
                dataId, group, content, ConfigType.JSON.getType());
            if (!ok) {
                log.info("推送路由到 Nacos 失败");
            } else {
                log.info("推送路由到 Nacos 成功，共 {} 条", list.size());
            }
            return ok;
        } catch (Exception ex) {
            log.error("推送路由到 Nacos Exception={}", ex);
        }
        return false;
    }
}
