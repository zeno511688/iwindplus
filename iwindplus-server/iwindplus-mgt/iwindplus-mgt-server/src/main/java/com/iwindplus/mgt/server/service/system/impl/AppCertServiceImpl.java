/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.AppCertDTO;
import com.iwindplus.mgt.domain.dto.system.AppCertSearchDTO;
import com.iwindplus.mgt.domain.vo.system.AppCertBaseVO;
import com.iwindplus.mgt.domain.vo.system.AppCertDataVO;
import com.iwindplus.mgt.domain.vo.system.AppCertPageVO;
import com.iwindplus.mgt.domain.vo.system.AppCertVO;
import com.iwindplus.mgt.server.dal.model.system.AppCertDO;
import com.iwindplus.mgt.server.dal.repository.system.AppCertRepository;
import com.iwindplus.mgt.server.service.asynccmd.AppCertTaskHandler;
import com.iwindplus.mgt.server.service.system.AppCertService;
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
 * 应用凭证业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_APP_CERT})
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class AppCertServiceImpl implements AppCertService {

    private final AppCertRepository appCertRepository;
    private final AsyncCmdExecutor asyncCmdExecutor;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(AppCertDTO entity) {
        entity.setAccessKey(IdUtil.simpleUUID());
        String secret = RandomUtil.randomString(32);
        entity.setSecretKey(secret);
        entity.setStatus(EnableStatusEnum.ENABLE);
        this.appCertRepository.getAppCertTypeIsExist(entity.getCertType());
        this.appCertRepository.getNameIsExist(entity.getName());
        this.appCertRepository.getAccessKeyIsExist(entity.getAccessKey());
        final AppCertDO model = BeanUtil.copyProperties(entity, AppCertDO.class);
        this.appCertRepository.save(model);
        entity.setId(model.getId());
        // 发送消息
        List<AppCertDataVO> result = List.of(
            AppCertDataVO.builder()
                .accessKey(entity.getAccessKey())
                .secretKey(entity.getSecretKey())
                .timeout(entity.getTimeout())
                .certType(entity.getCertType())
                .build());
        this.sendMsg(OperateTypeEnum.ADD, result);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<AppCertDO> list = this.appCertRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(AppCertDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.appCertRepository.removeByIds(ids);

        // 发送消息
        List<AppCertDataVO> result = list.stream()
            .map(entity -> AppCertDataVO.builder()
                .accessKey(entity.getAccessKey())
                .secretKey(entity.getSecretKey())
                .timeout(entity.getTimeout())
                .certType(entity.getCertType())
                .build())
            .collect(Collectors.toCollection(ArrayList::new));
        this.sendMsg(OperateTypeEnum.DELETE, result);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(AppCertDTO entity) {
        AppCertDO data = this.appCertRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.appCertRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey()) && !CharSequenceUtil.equals(data.getAccessKey(), entity.getAccessKey().trim())) {
            this.appCertRepository.getAccessKeyIsExist(entity.getAccessKey().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final AppCertDO model = BeanUtil.copyProperties(entity, AppCertDO.class);
        this.appCertRepository.updateById(model);

        // 发送消息
        List<AppCertDataVO> result = List.of(
            AppCertDataVO.builder()
                .accessKey(entity.getAccessKey())
                .secretKey(entity.getSecretKey())
                .timeout(entity.getTimeout())
                .certType(entity.getCertType())
                .build());
        if (EnableStatusEnum.DISABLE.equals(entity.getStatus())
            || EnableStatusEnum.LOCKED.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.DELETE, result);
        } else {
            this.sendMsg(OperateTypeEnum.MODIFY, result);
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        AppCertDO data = this.appCertRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        AppCertDO param = new AppCertDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.appCertRepository.updateById(param);

        // 发送消息
        List<AppCertDataVO> result = List.of(
            AppCertDataVO.builder()
                .accessKey(data.getAccessKey())
                .secretKey(data.getSecretKey())
                .timeout(data.getTimeout())
                .certType(data.getCertType())
                .build());
        if (EnableStatusEnum.DISABLE.equals(status)
            || EnableStatusEnum.LOCKED.equals(status)) {
            this.sendMsg(OperateTypeEnum.DELETE, result);
        } else {
            this.sendMsg(OperateTypeEnum.MODIFY, result);
        }
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        AppCertDO data = this.appCertRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        AppCertDO param = new AppCertDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.appCertRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public AppCertBaseVO editSecret(Long id) {
        final AppCertDO data = this.appCertRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        // 生成新的密钥
        String secret = RandomUtil.randomString(32);
        AppCertDO param = new AppCertDO();
        param.setId(data.getId());
        param.setSecretKey(secret);
        this.appCertRepository.updateById(param);

        // 发送消息
        List<AppCertDataVO> result = List.of(
            AppCertDataVO.builder()
                .accessKey(data.getAccessKey())
                .secretKey(data.getSecretKey())
                .timeout(data.getTimeout())
                .certType(data.getCertType())
                .build());
        this.sendMsg(OperateTypeEnum.MODIFY, result);

        return AppCertBaseVO.builder()
            .id(data.getId())
            .name(data.getName())
            .accessKey(data.getAccessKey())
            .secretKey(secret)
            .build();
    }

    @Override
    public IPage<AppCertPageVO> page(AppCertSearchDTO entity) {
        PageDTO<AppCertDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<AppCertDO> queryWrapper = Wrappers.lambdaQuery(AppCertDO.class)
            .orderByDesc(AppCertDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(AppCertDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(AppCertDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAccessKey())) {
            queryWrapper.like(AppCertDO::getAccessKey, entity.getAccessKey().trim());
        }
        queryWrapper.select(AppCertDO::getId, AppCertDO::getCreatedTime, AppCertDO::getCreatedTimestamp, AppCertDO::getCreatedBy,
            AppCertDO::getModifiedTime, AppCertDO::getModifiedTimestamp, AppCertDO::getModifiedBy, AppCertDO::getVersion, AppCertDO::getStatus,
            AppCertDO::getName, AppCertDO::getAccessKey, AppCertDO::getTimeout, AppCertDO::getCertType, AppCertDO::getBuildInFlag
        );
        final PageDTO<AppCertDO> modelPage = this.appCertRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, AppCertPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public AppCertVO getDetail(Long id) {
        AppCertDO data = this.appCertRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        AppCertVO result = BeanUtil.copyProperties(data, AppCertVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null", unless = "#result == null")
    @Override
    public BaseSignVO getByAccessKey(String accessKey, AppCertTypeEnum appCertType) {
        AppCertDO data = this.appCertRepository.getOne(Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getAccessKey, accessKey)
            .eq(AppCertDO::getCertType, appCertType));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BaseSignVO.builder()
            .accessKey(data.getAccessKey())
            .secretKey(data.getSecretKey())
            .timeout(data.getTimeout())
            .build();
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public BaseSignVO getByCertType(AppCertTypeEnum appCertType) {
        AppCertDO data = this.appCertRepository.getOne(Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getCertType, appCertType));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BaseSignVO.builder()
            .accessKey(data.getAccessKey())
            .secretKey(data.getSecretKey())
            .timeout(data.getTimeout())
            .build();
    }

    private boolean sendMsg(OperateTypeEnum operateType, List<AppCertDataVO> list) {
        if (CollUtil.isEmpty(list)) {
            return false;
        }

        List<BaseSignVO> dataList = list.stream()
            .filter(data -> AppCertTypeEnum.API_GATEWAY_SIGN_BLACKLIST.equals(data.getCertType()))
            .map(data -> BaseSignVO.builder()
                .accessKey(data.getAccessKey())
                .secretKey(data.getSecretKey())
                .timeout(data.getTimeout())
                .build())
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(dataList)) {
            return false;
        }

        MessageBaseDTO<List<BaseSignVO>> messageDTO = new MessageBaseDTO();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType(CharSequenceUtil.toCamelCase(AppCertTypeEnum.API_GATEWAY_SIGN_BLACKLIST.name()));
        messageDTO.setData(dataList);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncCmdExecutorBO build = AsyncCmdExecutorBO.builder()
            .bizType("APP_CERT")
            .eventType("APP_CERT_PUSH")
            .bizNumber(IdUtil.fastSimpleUUID())
            .content(ImmutableMap.of("content", content))
            .executorClass(AppCertTaskHandler.class)
            .remark("应用凭证数据发送kafka")
            .build();
        this.asyncCmdExecutor.submit(build);
        return true;
    }

}
