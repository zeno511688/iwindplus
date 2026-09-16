/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.security;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubmitDTO;
import com.iwindplus.base.async.task.executor.AsyncTaskExecutor;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.application.service.system.security.dto.IpBlackListChangeDTO;
import com.iwindplus.mgt.application.service.system.security.dto.IpBlackListDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.asynctask.security.ApiWhiteListTaskHandler;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * IP黑名单业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_IP_BLACK_LIST})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class IpBlackListApplicationService {

    private final IpBlackListRepository ipBlackListRepository;
    private final AsyncTaskExecutor asyncTaskExecutor;

    @CacheEvict(allEntries = true)
    public boolean save(IpBlackListDTO entity) {
        this.ipBlackListRepository.getIpIsExist(entity.getIp().trim());
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setSeq(this.ipBlackListRepository.getNextSeq());
        IpBlackListDO model = BeanUtil.copyProperties(entity, IpBlackListDO.class);
        this.ipBlackListRepository.save(model);
        entity.setId(model.getId());
        // 发送消息
        this.sendMsg(OperateTypeEnum.ADD, List.of(entity.getIp()), null);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<IpBlackListDO> list = this.ipBlackListRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.ipBlackListRepository.removeByIds(ids);

        // 发送消息
        final List<String> oldIp = list.stream().map(IpBlackListDO::getIp).collect(Collectors.toList());
        this.sendMsg(OperateTypeEnum.DELETE, null, oldIp);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean edit(IpBlackListDTO entity) {
        IpBlackListDO data = this.ipBlackListRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp()) && !CharSequenceUtil.equals(data.getIp(), entity.getIp().trim())) {
            this.ipBlackListRepository.getIpIsExist(entity.getIp().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        IpBlackListDO model = BeanUtil.copyProperties(entity, IpBlackListDO.class);
        this.ipBlackListRepository.updateById(model);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.MODIFY, List.of(entity.getIp()), List.of(data.getIp()));
        } else if (EnableStatusEnum.DISABLE.equals(entity.getStatus())
            || EnableStatusEnum.LOCKED.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getIp()));
        }

        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        IpBlackListDO data = this.ipBlackListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        IpBlackListDO entity = new IpBlackListDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.ipBlackListRepository.updateById(entity);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(status)) {
            this.sendMsg(OperateTypeEnum.ADD, List.of(data.getIp()), null);
        } else if (EnableStatusEnum.DISABLE.equals(status)
            || EnableStatusEnum.LOCKED.equals(status)) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getIp()));
        }

        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        IpBlackListDO data = this.ipBlackListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        IpBlackListDO param = new IpBlackListDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.ipBlackListRepository.updateById(param);
        return Boolean.TRUE;
    }

    private boolean sendMsg(OperateTypeEnum operateType, List<String> newIp, List<String> oldIp) {
        if (CollUtil.isEmpty(newIp)) {
            return false;
        }

        final MessageBaseDTO<IpBlackListChangeDTO> messageDTO = new MessageBaseDTO<>();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType("ipBlackList");

        IpBlackListChangeDTO ipBlackListChangeDTO = new IpBlackListChangeDTO();
        ipBlackListChangeDTO.setNewIp(newIp);
        if (CollUtil.isNotEmpty(oldIp)) {
            ipBlackListChangeDTO.setOldIp(oldIp);
        }
        messageDTO.setData(ipBlackListChangeDTO);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncTaskSubmitDTO build = AsyncTaskSubmitDTO.builder()
            .bizName("IP黑名单数据发送kafka")
            .bizKey("IP_BLACK_LIST")
            .bizType("IP_BLACK_PUSH")
            .param(ImmutableMap.of("content", content))
            .executorClass(ApiWhiteListTaskHandler.class)
            .remark("IP黑名单数据发送kafka")
            .build();
        this.asyncTaskExecutor.submit(build);
        return true;
    }
}
