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
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.IpBlackListChangeDTO;
import com.iwindplus.mgt.domain.dto.system.IpBlackListDTO;
import com.iwindplus.mgt.domain.dto.system.IpBlackListSearchDTO;
import com.iwindplus.mgt.domain.vo.system.IpBlackListPageVO;
import com.iwindplus.mgt.domain.vo.system.IpBlackListVO;
import com.iwindplus.mgt.server.dal.model.system.IpBlackListDO;
import com.iwindplus.mgt.server.dal.repository.system.IpBlackListRepository;
import com.iwindplus.mgt.server.service.asynccmd.ApiWhiteListTaskHandler;
import com.iwindplus.mgt.server.service.system.IpBlackListService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class IpBlackListServiceImpl implements IpBlackListService {

    private final IpBlackListRepository ipBlackListRepository;
    private final AsyncCmdExecutor asyncCmdExecutor;

    @CacheEvict(allEntries = true)
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public IPage<IpBlackListPageVO> page(IpBlackListSearchDTO entity) {
        PageDTO<IpBlackListDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<IpBlackListDO> queryWrapper = Wrappers.lambdaQuery(IpBlackListDO.class)
            .orderByDesc(IpBlackListDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(IpBlackListDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            queryWrapper.like(IpBlackListDO::getIp, entity.getIp().trim());
        }
        final PageDTO<IpBlackListDO> modelPage = this.ipBlackListRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, IpBlackListPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<String> listIp() {
        LambdaQueryWrapper<IpBlackListDO> queryWrapper = Wrappers.lambdaQuery(IpBlackListDO.class)
            .eq(IpBlackListDO::getStatus, EnableStatusEnum.ENABLE)
            .select(IpBlackListDO::getIp)
            .orderByAsc(List.of(IpBlackListDO::getIp));
        List<IpBlackListDO> list = this.ipBlackListRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.stream().filter(Objects::nonNull).map(IpBlackListDO::getIp).distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public IpBlackListVO getDetail(Long id) {
        IpBlackListDO data = this.ipBlackListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, IpBlackListVO.class);
    }

    private boolean sendMsg(OperateTypeEnum operateType, List<String> newIp, List<String> oldIp) {
        if (CollUtil.isEmpty(newIp)) {
            return false;
        }

        final MessageBaseDTO<IpBlackListChangeDTO> messageDTO = new MessageBaseDTO();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType("ipBlackList");

        IpBlackListChangeDTO ipBlackListChangeDTO = new IpBlackListChangeDTO();
        ipBlackListChangeDTO.setNewIp(newIp);
        if (CollUtil.isNotEmpty(oldIp)) {
            ipBlackListChangeDTO.setOldIp(oldIp);
        }
        messageDTO.setData(ipBlackListChangeDTO);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncCmdExecutorBO build = AsyncCmdExecutorBO.builder()
            .bizType("IP_BLACK_LIST")
            .eventType("IP_BLACK_PUSH")
            .bizNumber(IdUtil.fastSimpleUUID())
            .content(ImmutableMap.of("content", content))
            .executorClass(ApiWhiteListTaskHandler.class)
            .remark("IP黑名单数据发送kafka")
            .build();
        this.asyncCmdExecutor.submit(build);
        return true;
    }
}
