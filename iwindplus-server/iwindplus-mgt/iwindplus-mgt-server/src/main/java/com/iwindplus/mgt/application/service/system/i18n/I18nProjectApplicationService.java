/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.i18n;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskExtDTO;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubmitDTO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubmitVO;
import com.iwindplus.base.async.task.executor.AsyncTaskExecutor;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.i18n.domain.constant.I18nConstant;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.mgt.application.query.system.i18n.dto.I18nMsgQueryDTO;
import com.iwindplus.mgt.application.query.system.i18n.vo.I18nMsgExtendVO;
import com.iwindplus.mgt.application.service.system.i18n.dto.I18nProjectExtendDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.asynctask.i18n.I18nMsgPushTaskHandler;
import com.iwindplus.mgt.infrastructure.asynctask.i18n.I18nMsgRemoveTaskHandler;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nMsgRepository;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nProjectDO;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nProjectRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
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
public class I18nProjectApplicationService {

    private final I18nProjectRepository i18nProjectRepository;
    private final I18nMsgRepository i18nMsgRepository;
    private final RedissonExecutor redissonExecutor;
    private final AsyncTaskExecutor asyncTaskExecutor;

    @CacheEvict(allEntries = true)
    public boolean save(I18nProjectExtendDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        if (!entity.getFileName().endsWith(I18nConstant.FILE_SUFFIX)) {
            throw new BizException(MgtCodeEnum.I18N_FILE_SUFFIX_ERROR);
        }
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.I18N_PROJECT_PREFIX.getValue()));
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
        final AsyncTaskSubmitDTO build = AsyncTaskSubmitDTO.builder()
            .bizName("国际化消息推送数据至Nacos")
            .bizKey("I18N_MSG")
            .bizType("I18N_MSG_PUSH")
            .param(ImmutableMap.of("fileName", fileName, "content", content))
            .executorClass(I18nMsgPushTaskHandler.class)
            .remark("国际化消息推送数据至Nacos")
            .ext(AsyncTaskExtDTO.builder().enabledSuccessDelete(Boolean.TRUE).build())
            .build();
        final AsyncTaskSubmitVO submit = this.asyncTaskExecutor.submit(build);
        log.info("Push i18n data to nacos, fileName: {}, result: {}", fileName, submit);
        return true;
    }

    private boolean removeData(String fileName) {
        final AsyncTaskSubmitDTO build = AsyncTaskSubmitDTO.builder()
            .bizName("删除Nacos国际化消息数据")
            .bizKey("I18N_MSG")
            .bizType("I18N_MSG_REMOVE")
            .param(ImmutableMap.of("fileName", fileName))
            .executorClass(I18nMsgRemoveTaskHandler.class)
            .remark("删除Nacos国际化消息数据")
            .ext(AsyncTaskExtDTO.builder().enabledSuccessDelete(Boolean.TRUE).build())
            .build();
        this.asyncTaskExecutor.submit(build);
        return true;
    }

}
