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
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.SmsTplEditDTO;
import com.iwindplus.setup.domain.dto.SmsTplSaveDTO;
import com.iwindplus.setup.domain.dto.SmsTplSearchDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.SmsTplPageVO;
import com.iwindplus.setup.domain.vo.SmsTplVO;
import com.iwindplus.setup.server.dal.model.SmsTplDO;
import com.iwindplus.setup.server.dal.repository.SmsTplRepository;
import com.iwindplus.setup.server.service.SmsTplService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短信模板业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SMS_TPL})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SmsTplServiceImpl implements SmsTplService {

    private final SmsTplRepository smsTplRepository;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(SmsTplSaveDTO entity) {
        this.smsTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final SmsTplDO model = BeanUtil.copyProperties(entity, SmsTplDO.class);
        this.buildDefault(model);
        this.smsTplRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<SmsTplDO> list = this.smsTplRepository.listByIds(ids);
        if (Objects.isNull(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(SmsTplDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.smsTplRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(SmsTplEditDTO entity) {
        // 编辑
        SmsTplDO data = this.smsTplRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.smsTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.smsTplRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final SmsTplDO model = BeanUtil.copyProperties(entity, SmsTplDO.class);
        this.buildDefault(model);
        this.smsTplRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        SmsTplDO data = this.smsTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        SmsTplDO entity = new SmsTplDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.smsTplRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        SmsTplDO data = this.smsTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        SmsTplDO param = new SmsTplDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.smsTplRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<SmsTplPageVO> page(PageDTO<SmsTplDO> page, SmsTplSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<SmsTplDO> queryWrapper = Wrappers.lambdaQuery(SmsTplDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(SmsTplDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(SmsTplDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(SmsTplDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getSignName())) {
            queryWrapper.eq(SmsTplDO::getSignName, entity.getSignName().trim());
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
        queryWrapper.select(SmsTplDO::getId, SmsTplDO::getCreatedTime, SmsTplDO::getCreatedTimestamp, SmsTplDO::getCreatedBy,
            SmsTplDO::getModifiedTime, SmsTplDO::getModifiedTimestamp, SmsTplDO::getModifiedBy, SmsTplDO::getBuildInFlag, SmsTplDO::getVersion,
            SmsTplDO::getRemark, SmsTplDO::getStatus, SmsTplDO::getCode, SmsTplDO::getName, SmsTplDO::getSignName
        );
        final PageDTO<SmsTplDO> modelPage = this.smsTplRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, SmsTplPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public SmsTplVO getByCode(String code) {
        SmsTplDO data = this.smsTplRepository.getOne(Wrappers.lambdaQuery(SmsTplDO.class)
            .eq(SmsTplDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.SMS_TEMPLATE_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.SMS_TEMPLATE_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.SMS_TEMPLATE_LOCKED);
        }
        final SmsTplVO result = BeanUtil.copyProperties(data, SmsTplVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public SmsTplVO getDetail(Long id) {
        SmsTplDO data = this.smsTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final SmsTplVO result = BeanUtil.copyProperties(data, SmsTplVO.class);
        return result;
    }

    private void buildDefault(SmsTplDO entity) {
        if (Objects.isNull(entity.getCaptchaLength())) {
            entity.setCaptchaLength(SmsConstant.CAPTCHA_LENGTH);
        }
        if (Objects.isNull(entity.getCaptchaTimeout())) {
            entity.setCaptchaTimeout(SmsConstant.CAPTCHA_TIMEOUT);
        }
        if (Objects.isNull(entity.getLimitCountDay())) {
            entity.setLimitCountDay(NumberConstant.NUMBER_TWENTY);
        }
        if (Objects.isNull(entity.getLimitCountHour())) {
            entity.setLimitCountHour(NumberConstant.NUMBER_FIVE);
        }
        if (Objects.isNull(entity.getLimitCountMinute())) {
            entity.setLimitCountMinute(NumberConstant.NUMBER_ONE);
        }
    }
}
