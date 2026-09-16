/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.organization;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgAuditDTO;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgEditDTO;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgExtendDTO;
import com.iwindplus.mgt.application.service.upms.organization.dto.OrgSaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgAuditRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgExtendRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserOrgRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.common.enums.OrgAuditStatusEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ORG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OrgApplicationService {

    private final RedissonExecutor redissonExecutor;
    private final OssClient ossClient;
    private final UserOrgRepository userOrgRepository;
    private final OrgExtendRepository orgExtendRepository;
    private final OrgAuditRepository orgAuditRepository;
    private final OrgRepository orgRepository;
    private final MgtProperty property;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean save(OrgSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setAuditStatus(OrgAuditStatusEnum.NEW_BUILT);
        this.orgRepository.getNameIsExist(entity.getName().trim());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.ORG_PREFIX.getValue()));
        }
        this.orgRepository.getCodeIsExist(entity.getCode().trim());
        entity.setSeq(this.orgRepository.getNextSeq());
        final OrgDO model = BeanUtil.copyProperties(entity, OrgDO.class);
        this.orgRepository.save(model);
        entity.setId(model.getId());
        // 扩展字段
        OrgExtendDTO orgExtend = OrgExtendDTO.builder()
            .intro(entity.getIntro())
            .orgId(entity.getId())
            .build();
        this.orgExtendRepository.save(orgExtend);
        // 默认审核状态为新建
        OrgAuditDTO build = OrgAuditDTO
            .builder()
            .orgId(entity.getId())
            .auditStatus(OrgAuditStatusEnum.NEW_BUILT)
            .remark(OrgAuditStatusEnum.NEW_BUILT.getDesc())
            .build();
        this.orgAuditRepository.save(build);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<OrgDO> list = this.orgRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().anyMatch(OrgDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.orgExtendRepository.getBaseMapper().deleteByOrgIds(ids);
        this.orgAuditRepository.getBaseMapper().deleteByOrgIds(ids);
        this.userOrgRepository.getBaseMapper().deleteByOrgIds(ids);
        this.orgRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean edit(OrgEditDTO entity) {
        OrgDO data = this.orgRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 新建或已驳回状态才能编辑
        if (!(OrgAuditStatusEnum.NEW_BUILT.equals(data.getAuditStatus()) || OrgAuditStatusEnum.REJECTED.equals(data.getAuditStatus()))) {
            throw new BizException(MgtCodeEnum.NEW_AND_REJECTED_CAN_EDIT);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.orgRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.orgRepository.getCodeIsExist(entity.getCode().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final OrgDO model = BeanUtil.copyProperties(entity, OrgDO.class);
        this.orgRepository.updateById(model);
        // 扩展字段
        OrgExtendDTO orgExtend = OrgExtendDTO.builder()
            .intro(entity.getIntro())
            .orgId(entity.getId())
            .build();
        this.orgExtendRepository.edit(orgExtend);
        this.removeOldPic(entity, data);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean editAuditStatus(OrgAuditDTO entity) {
        OrgDO data = this.orgRepository.getById(entity.getOrgId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (entity.getAuditStatus().equals(data.getAuditStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        this.orgAuditRepository.save(entity);
        OrgDO param = new OrgDO();
        param.setId(entity.getOrgId());
        param.setAuditStatus(entity.getAuditStatus());
        param.setVersion(data.getVersion());
        this.orgRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean editStatus(Long id, EnableStatusEnum status) {
        OrgDO data = this.orgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OrgDO param = new OrgDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.orgRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true)
        }
    )
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        OrgDO data = this.orgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OrgDO param = new OrgDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.orgRepository.updateById(param);
        return Boolean.TRUE;
    }

    private void removeOldPic(OrgEditDTO entity, OrgDO data) {
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(entity.getLogo())
            && CharSequenceUtil.isNotBlank(data.getLogo())
            && !CharSequenceUtil.equals(data.getLogo(), entity.getLogo().trim())) {
            relativePaths.add(data.getLogo());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBusinessLicense()) && CharSequenceUtil.isNotBlank(data.getBusinessLicense())
            && !CharSequenceUtil.equals(data.getBusinessLicense(), entity.getBusinessLicense().trim())) {
            relativePaths.add(data.getBusinessLicense());
        }
        OrgApplicationService.removeFiles(this.ossClient, this.property.getOss().getCode(), this.property.getOss().getTplCode(), relativePaths);
    }

    public static void removeFiles(OssClient ossClient, String code, String ossTplCode, List<String> relativePaths) {
        if (CharSequenceUtil.isBlank(ossTplCode) || CollUtil.isEmpty(relativePaths)) {
            return;
        }
        try {
            ossClient.removeFiles(code, ossTplCode, relativePaths);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
    }

    public static List<FilePathVO> getFilePaths(
        String code,
        String ossTplCode,
        List<String> relativePaths,
        OssClient ossClient) {
        if (CharSequenceUtil.isBlank(code) || CharSequenceUtil.isBlank(ossTplCode) || CollUtil.isEmpty(relativePaths)) {
            return null;
        }
        try {
            return Optional.ofNullable(ossClient.listSignUrl(code, ossTplCode, relativePaths, null))
                .map(ResultVO::getBizData).orElse(null);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

}
