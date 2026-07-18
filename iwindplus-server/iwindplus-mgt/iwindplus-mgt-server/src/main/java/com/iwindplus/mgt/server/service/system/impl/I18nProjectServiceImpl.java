/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseVO;
import com.iwindplus.base.i18n.domain.constant.I18nConstant;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.I18nMsgQueryDTO;
import com.iwindplus.mgt.domain.dto.system.I18nProjectExtendDTO;
import com.iwindplus.mgt.domain.dto.system.I18nProjectSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.system.I18nMsgExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nProjectExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nProjectPageVO;
import com.iwindplus.mgt.server.dal.model.system.I18nProjectDO;
import com.iwindplus.mgt.server.dal.repository.system.I18nMsgRepository;
import com.iwindplus.mgt.server.dal.repository.system.I18nProjectRepository;
import com.iwindplus.mgt.server.service.asynccmd.I18nMsgPushTaskHandler;
import com.iwindplus.mgt.server.service.asynccmd.I18nMsgRemoveTaskHandler;
import com.iwindplus.mgt.server.service.system.I18nProjectService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 国际化项目业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_I18N_PROJECT})
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class I18nProjectServiceImpl implements I18nProjectService {

    private final I18nProjectRepository i18nProjectRepository;
    private final I18nMsgRepository i18nMsgRepository;
    private final RedissonService redissonService;
    private final AsyncCmdExecutor asyncCmdExecutor;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(I18nProjectExtendDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        if (!entity.getFileName().endsWith(I18nConstant.FILE_SUFFIX)) {
            throw new BizException(MgtCodeEnum.I18N_FILE_SUFFIX_ERROR);
        }
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.I18N_PROJECT_PREFIX.getValue()));
        }
        this.i18nProjectRepository.getCodeIsExist(entity.getPlatformType(), entity.getCode().trim());
        this.i18nProjectRepository.getNameIsExist(entity.getPlatformType(), entity.getName().trim());
        this.i18nProjectRepository.getFileNameIsExist(entity.getPlatformType(), entity.getFileName().trim());
        entity.setSeq(this.i18nProjectRepository.getNextSeq(entity.getPlatformType()));
        final I18nProjectDO model = this.i18nProjectRepository.saveOrUpdateI18nProject(entity);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<I18nProjectDO> list = this.i18nProjectRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(I18nProjectDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.i18nProjectRepository.removeByProjectIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(I18nProjectExtendDTO entity) {
        I18nProjectDO data = this.i18nProjectRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (!entity.getFileName().endsWith(I18nConstant.FILE_SUFFIX)) {
            throw new BizException(MgtCodeEnum.I18N_FILE_SUFFIX_ERROR);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.i18nProjectRepository.getNameIsExist(entity.getPlatformType(), entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getFileName()) && !CharSequenceUtil.equals(data.getFileName(), entity.getFileName().trim())) {
            this.i18nProjectRepository.getFileNameIsExist(entity.getPlatformType(), entity.getFileName().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        this.i18nProjectRepository.saveOrUpdateI18nProject(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        I18nProjectDO data = this.i18nProjectRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        I18nProjectDO param = new I18nProjectDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.i18nProjectRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        I18nProjectDO data = this.i18nProjectRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        I18nProjectDO param = new I18nProjectDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.i18nProjectRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<I18nProjectPageVO> page(I18nProjectSearchDTO entity) {
        PageDTO<I18nProjectDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<I18nProjectDO> queryWrapper = Wrappers.lambdaQuery(I18nProjectDO.class)
            .orderByDesc(I18nProjectDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(I18nProjectDO::getStatus, entity.getStatus());
        }
        if (Objects.nonNull(entity.getPlatformType())) {
            queryWrapper.eq(I18nProjectDO::getPlatformType, entity.getPlatformType());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(I18nProjectDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getFileName())) {
            queryWrapper.like(I18nProjectDO::getFileName, entity.getFileName().trim());
        }
        queryWrapper.select(I18nProjectDO::getId, I18nProjectDO::getCreatedTime, I18nProjectDO::getCreatedTimestamp, I18nProjectDO::getCreatedBy,
            I18nProjectDO::getModifiedTime, I18nProjectDO::getModifiedTimestamp, I18nProjectDO::getModifiedBy, I18nProjectDO::getVersion,
            I18nProjectDO::getStatus,
            I18nProjectDO::getPlatformType, I18nProjectDO::getName, I18nProjectDO::getFileName, I18nProjectDO::getSeq, I18nProjectDO::getBuildInFlag
        );
        final PageDTO<I18nProjectDO> modelPage = this.i18nProjectRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, I18nProjectPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public I18nProjectExtendVO getDetail(Long id) {
        final I18nProjectDO data = this.i18nProjectRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final I18nProjectExtendVO result = BeanUtil.copyProperties(data, I18nProjectExtendVO.class);
        final I18nMsgQueryDTO queryDTO = I18nMsgQueryDTO
            .builder()
            .projectId(id)
            .build();
        final List<I18nMsgExtendVO> list = this.i18nMsgRepository.listByCondition(queryDTO);
        if (CollUtil.isNotEmpty(list)) {
            result.setContent(this.i18nMsgRepository.buildContent(list));
        }
        return result;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<BaseVO> listEnabled() {
        LambdaQueryWrapper<I18nProjectDO> queryWrapper = Wrappers.lambdaQuery(I18nProjectDO.class)
            .eq(I18nProjectDO::getStatus, EnableStatusEnum.ENABLE)
            .select(I18nProjectDO::getId, I18nProjectDO::getCode, I18nProjectDO::getName)
            .orderByAsc(List.of(I18nProjectDO::getSeq));
        List<I18nProjectDO> list = this.i18nProjectRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, BaseVO.class);
    }

    @Override
    public boolean pushData(Long id) {
        final I18nMsgQueryDTO queryDTO = I18nMsgQueryDTO
            .builder()
            .projectId(id)
            .build();
        final List<I18nMsgExtendVO> list = this.i18nMsgRepository.listByCondition(queryDTO);
        if (CollUtil.isEmpty(list)) {
            return false;
        }

        final I18nMsgExtendVO data = list.get(0);
        if (!PlatformTypeEnum.MGT.equals(data.getProjectPlatformType())) {
            return false;
        }

        if (EnableStatusEnum.ENABLE.equals(data.getProjectStatus())) {
            final String content = this.i18nMsgRepository.buildContent(list);
            if (CharSequenceUtil.isBlank(content)) {
                return false;
            }
            return this.pushData(data.getProjectFileName(), content);
        } else {
            return this.removeData(data.getProjectFileName());
        }
    }

    @Override
    public void download(Long id, HttpServletResponse response) {
        final I18nMsgQueryDTO queryDTO = I18nMsgQueryDTO
            .builder()
            .projectId(id)
            .projectStatus(EnableStatusEnum.ENABLE)
            .build();
        final List<I18nMsgExtendVO> list = this.i18nMsgRepository.listByCondition(queryDTO);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final I18nMsgExtendVO data = list.get(0);
        final String content = this.i18nMsgRepository.buildContent(list);
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            FilesUtil.downloadFile(inputStream, data.getProjectFileName(), response);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        loadDataPush().subscribe();
    }

    private Mono<Void> loadDataPush() {
        I18nMsgQueryDTO queryDTO = I18nMsgQueryDTO.builder()
            .projectPlatformType(PlatformTypeEnum.MGT)
            .projectStatus(EnableStatusEnum.ENABLE)
            .msgStatus(EnableStatusEnum.ENABLE)
            .build();

        return Mono.fromSupplier(() -> i18nMsgRepository.listByCondition(queryDTO))
            .subscribeOn(Schedulers.boundedElastic())
            .filter(CollUtil::isNotEmpty)
            .flatMapMany(list -> Flux.fromIterable(list)
                .groupBy(I18nMsgExtendVO::getProjectId)
                .flatMap(group -> group.collectList()
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(voList -> {
                        I18nMsgExtendVO firstVo = voList.get(0);
                        if (!PlatformTypeEnum.MGT.equals(firstVo.getProjectPlatformType())) {
                            return Mono.empty();
                        }
                        String content = i18nMsgRepository.buildContent(voList);
                        if (CharSequenceUtil.isBlank(content)) {
                            return Mono.empty();
                        }

                        return Mono.fromRunnable(() ->
                            pushData(firstVo.getProjectFileName(), content)
                        ).subscribeOn(Schedulers.boundedElastic());
                    })
                )
            )
            .then()
            .doOnSuccess(v -> log.info("All i18n data pushed successfully"))
            .doOnError(error -> log.error("Error in i18n data push flow", error));
    }

    private boolean pushData(String fileName, String content) {
        final AsyncCmdExecutorBO build = AsyncCmdExecutorBO.builder()
            .bizType("I18N_MSG")
            .eventType("I18N_MSG_PUSH")
            .bizNumber(IdUtil.fastSimpleUUID())
            .content(ImmutableMap.of("fileName", fileName, "content", content))
            .executorClass(I18nMsgPushTaskHandler.class)
            .remark("国际化消息推送数据至Nacos")
            .build();
        this.asyncCmdExecutor.submit(build);
        return true;
    }

    private boolean removeData(String fileName) {
        final AsyncCmdExecutorBO build = AsyncCmdExecutorBO.builder()
            .bizType("I18N_MSG")
            .eventType("I18N_MSG_REMOVE")
            .bizNumber(IdUtil.fastSimpleUUID())
            .content(ImmutableMap.of("fileName", fileName))
            .executorClass(I18nMsgRemoveTaskHandler.class)
            .remark("删除Nacos国际化消息数据")
            .build();
        this.asyncCmdExecutor.submit(build);
        return true;
    }

}
