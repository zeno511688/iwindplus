/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.organization;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.application.query.upms.organization.dto.OrgSearchDTO;
import com.iwindplus.mgt.application.query.upms.organization.vo.OrgExtendVO;
import com.iwindplus.mgt.application.query.upms.organization.vo.OrgPageVO;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgExtendRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.api.upms.vo.OrgBaseCheckedVO;
import com.iwindplus.mgt.api.upms.vo.OrgVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 组织查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ORG})
@RequiredArgsConstructor
public class OrgQueryService {

    private final OssClient ossClient;
    private final OrgExtendRepository orgExtendRepository;
    private final OrgRepository orgRepository;
    private final MgtProperty property;

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
            OrderItem item = OrderItem.desc(DbConstant.MODIFIED_TIMESTAMP);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(OrgDO::getId, OrgDO::getCreatedTimestamp, OrgDO::getCreatedBy,
            OrgDO::getModifiedTimestamp, OrgDO::getModifiedBy, OrgDO::getVersion, OrgDO::getStatus, OrgDO::getAuditStatus, OrgDO::getCode,
            OrgDO::getName, OrgDO::getAbbr, OrgDO::getUscc, OrgDO::getCountry, OrgDO::getProvince, OrgDO::getCity, OrgDO::getDistrict,
            OrgDO::getBuildInFlag
        );
        final PageDTO<OrgDO> modelPage = this.orgRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, OrgPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public List<OrgBaseCheckedVO> listByUserId(Long userId) {
        return this.orgRepository.getBaseMapper().selectListByUserId(userId);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public OrgVO getDetail(Long id) {
        OrgVO data = this.orgRepository.getBaseMapper().selectDetailById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data;
    }

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
        List<FilePathVO> filePaths = OrgApplicationService.getFilePaths(
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths,
            this.ossClient);
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

    @Cacheable(key = "#root.methodName + '_' + #p0 ", condition = "#p0 != null", unless = "#result == null")
    public OrgBaseCheckedVO getOrg(Long userId) {
        List<OrgBaseCheckedVO> list = this.listByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        List<OrgBaseCheckedVO> resultList = list.stream().filter(OrgBaseCheckedVO::getChecked).collect(Collectors.toCollection(ArrayList::new));
        if (CollUtil.isEmpty(resultList)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        return resultList.get(0);
    }

    public Long getOrgId(Long userId) {
        final OrgBaseCheckedVO org = this.orgRepository.getOrg(userId);
        if (Objects.isNull(org)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        return org.getId();
    }
}
