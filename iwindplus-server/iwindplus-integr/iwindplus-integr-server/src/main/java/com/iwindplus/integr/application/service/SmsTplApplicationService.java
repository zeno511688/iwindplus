/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.integr.common.constant.IntegrConstant.RedisCacheConstant;
import com.iwindplus.integr.application.service.dto.SmsTplEditDTO;
import com.iwindplus.integr.application.service.dto.SmsTplSaveDTO;
import com.iwindplus.integr.infrastructure.persistence.SmsTplDO;
import com.iwindplus.integr.infrastructure.persistence.SmsTplRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短信模板业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SMS_TPL})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SmsTplApplicationService {

    private final SmsTplRepository smsTplRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
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

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
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

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
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

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    @CacheEvict(allEntries = true)
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

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    @CacheEvict(allEntries = true)
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
