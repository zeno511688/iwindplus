/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.OrgAuditDTO;
import com.iwindplus.mgt.domain.dto.power.OrgEditDTO;
import com.iwindplus.mgt.domain.dto.power.OrgExtendDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSaveDTO;
import com.iwindplus.mgt.domain.dto.power.OrgSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.enums.OrgAuditStatusEnum;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.OrgExtendVO;
import com.iwindplus.mgt.domain.vo.power.OrgPageVO;
import com.iwindplus.mgt.domain.vo.power.OrgVO;
import com.iwindplus.mgt.server.config.property.MgtProperty;
import com.iwindplus.mgt.server.dal.model.power.OrgDO;
import com.iwindplus.mgt.server.dal.repository.power.OrgAuditRepository;
import com.iwindplus.mgt.server.dal.repository.power.OrgExtendRepository;
import com.iwindplus.mgt.server.dal.repository.power.OrgRepository;
import com.iwindplus.mgt.server.dal.repository.power.UserOrgRepository;
import com.iwindplus.mgt.server.service.power.OrgService;
import com.iwindplus.setup.client.OssClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class OrgServiceImpl implements OrgService {

    private final RedissonService redissonService;
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
    @Override
    public boolean save(OrgSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setAuditStatus(OrgAuditStatusEnum.NEW_BUILT);
        this.orgRepository.getNameIsExist(entity.getName().trim());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.ORG_PREFIX.getValue()));
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public IPage<OrgPageVO> page(OrgSearchDTO entity) {
        final PageDTO<OrgDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<OrgDO> queryWrapper = Wrappers.lambdaQuery(OrgDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(OrgDO::getStatus, entity.getStatus());
        }
        if (Objects.nonNull(entity.getAuditStatus())) {
            queryWrapper.eq(OrgDO::getAuditStatus, entity.getAuditStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(OrgDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(OrgDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAbbr())) {
            queryWrapper.eq(OrgDO::getAbbr, entity.getAbbr().trim());
        }
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(CommonConstant.DbConstant.MODIFIED_TIME);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(OrgDO::getId, OrgDO::getCreatedTime, OrgDO::getCreatedTimestamp, OrgDO::getCreatedBy, OrgDO::getModifiedTime,
            OrgDO::getModifiedTimestamp, OrgDO::getModifiedBy, OrgDO::getVersion, OrgDO::getStatus, OrgDO::getAuditStatus, OrgDO::getCode,
            OrgDO::getName, OrgDO::getAbbr, OrgDO::getUscc, OrgDO::getCountry, OrgDO::getProvince, OrgDO::getCity, OrgDO::getDistrict,
            OrgDO::getBuildInFlag
        );
        final PageDTO<OrgDO> modelPage = this.orgRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, OrgPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<OrgBaseCheckedVO> listByUserId(Long userId) {
        return this.orgRepository.getBaseMapper().selectListByUserId(userId);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public OrgVO getDetail(Long id) {
        OrgVO data = this.orgRepository.getBaseMapper().selectDetailById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data;
    }

    @Override
    public OrgExtendVO getDetailExtend(Long id) {
        final OrgVO data = this.getDetail(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        OrgExtendVO result = BeanUtil.copyProperties(data, OrgExtendVO.class);
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getBusinessLicense())) {
            relativePaths.add(data.getBusinessLicense());
        }
        if (CharSequenceUtil.isNotBlank(data.getLogo())) {
            relativePaths.add(data.getLogo());
        }
        List<FilePathVO> filePaths = OrgServiceImpl.getFilePaths(this.property.getOss().getTplCode(), relativePaths, this.ossClient);
        if (CollUtil.isNotEmpty(filePaths)) {
            filePaths.forEach(p -> {
                if (CharSequenceUtil.isNotBlank(data.getBusinessLicense()) && data.getBusinessLicense().equals(p.getRelativePath())) {
                    result.setBusinessLicenseStr(p.getAbsolutePath());
                }
                if (CharSequenceUtil.isNotBlank(data.getLogo()) && data.getLogo().equals(p.getRelativePath())) {
                    result.setLogoStr(p.getAbsolutePath());
                }
            });
        }
        final String intro = this.orgExtendRepository.getIntroByOrgId(id);
        result.setIntro(intro);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 ", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    @Override
    public OrgBaseCheckedVO getOrg(Long userId) {
        List<OrgBaseCheckedVO> list = this.listByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        List<OrgBaseCheckedVO> resultList = list.stream().filter(m -> m.getChecked()).collect(Collectors.toCollection(ArrayList::new));
        if (CollUtil.isEmpty(resultList)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        return resultList.get(0);
    }

    @Override
    public Long getOrgId(Long userId) {
        final OrgBaseCheckedVO org = this.orgRepository.getOrg(userId);
        if (Objects.isNull(org)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        return org.getId();
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
        OrgServiceImpl.removeFiles(this.ossClient, this.property.getOss().getTplCode(), relativePaths);
    }

    static void removeFiles(OssClient ossClient, String ossTplCode, List<String> relativePaths) {
        if (CharSequenceUtil.isBlank(ossTplCode) || CollUtil.isEmpty(relativePaths)) {
            return;
        }
        try {
            ossClient.removeFiles(ossTplCode, relativePaths);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
    }

    static List<FilePathVO> getFilePaths(String ossTplCode, List<String> relativePaths, OssClient ossClient) {
        if (CharSequenceUtil.isBlank(ossTplCode) || CollUtil.isEmpty(relativePaths)) {
            return null;
        }
        try {
            return Optional.ofNullable(ossClient.listSignUrl(ossTplCode, relativePaths, null))
                .map(ResultVO::getBizData).orElse(null);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

}
