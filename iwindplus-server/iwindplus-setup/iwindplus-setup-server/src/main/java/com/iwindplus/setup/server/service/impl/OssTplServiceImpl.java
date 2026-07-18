/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.OssTplEditDTO;
import com.iwindplus.setup.domain.dto.OssTplSaveDTO;
import com.iwindplus.setup.domain.dto.OssTplSearchDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.OssTplPageVO;
import com.iwindplus.setup.domain.vo.OssTplVO;
import com.iwindplus.setup.server.dal.model.OssTplDO;
import com.iwindplus.setup.server.dal.repository.OssTplRepository;
import com.iwindplus.setup.server.service.OssTplService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对象存储模板业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_OSS_TPL})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OssTplServiceImpl implements OssTplService {

    private final OssTplRepository ossTplRepository;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(OssTplSaveDTO entity) {
        this.ossTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        this.ossTplRepository.getBucketNameIsExist(entity.getBucketName().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final OssTplDO model = BeanUtil.copyProperties(entity, OssTplDO.class);
        this.ossTplRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<OssTplDO> list = this.ossTplRepository.listByIds(ids);
        if (Objects.isNull(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(OssTplDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.ossTplRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(OssTplEditDTO entity) {
        // 编辑
        OssTplDO data = this.ossTplRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.ossTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.ossTplRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBucketName()) && !CharSequenceUtil.equals(data.getBucketName(), entity.getBucketName().trim())) {
            this.ossTplRepository.getBucketNameIsExist(entity.getBucketName().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final OssTplDO model = BeanUtil.copyProperties(entity, OssTplDO.class);
        this.ossTplRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        OssTplDO data = this.ossTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OssTplDO entity = new OssTplDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.ossTplRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        OssTplDO data = this.ossTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        OssTplDO param = new OssTplDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.ossTplRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<OssTplPageVO> page(PageDTO<OssTplDO> page, OssTplSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<OssTplDO> queryWrapper = Wrappers.lambdaQuery(OssTplDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(OssTplDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(OssTplDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(OssTplDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBucketName())) {
            queryWrapper.eq(OssTplDO::getBucketName, entity.getBucketName().trim());
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
        queryWrapper.select(OssTplDO::getId, OssTplDO::getCreatedTime, OssTplDO::getCreatedTimestamp, OssTplDO::getCreatedBy,
            OssTplDO::getModifiedTime, OssTplDO::getModifiedTimestamp, OssTplDO::getModifiedBy, OssTplDO::getBuildInFlag, OssTplDO::getVersion,
            OssTplDO::getRemark, OssTplDO::getStatus, OssTplDO::getCode, OssTplDO::getName, OssTplDO::getBucketName
        );
        final PageDTO<OssTplDO> modelPage = this.ossTplRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, OssTplPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public OssTplVO getByCode(String code) {
        OssTplDO data = this.ossTplRepository.getOne(Wrappers.lambdaQuery(OssTplDO.class)
            .eq(OssTplDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.OSS_TEMPLATE_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.OSS_TEMPLATE_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.OSS_TEMPLATE_LOCKED);
        }
        final OssTplVO result = BeanUtil.copyProperties(data, OssTplVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public OssTplVO getDetail(Long id) {
        OssTplDO data = this.ossTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final OssTplVO result = BeanUtil.copyProperties(data, OssTplVO.class);
        return result;
    }

}
