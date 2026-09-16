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
import com.iwindplus.integr.application.service.dto.MailTplEditDTO;
import com.iwindplus.integr.application.service.dto.MailTplSaveDTO;
import com.iwindplus.integr.infrastructure.persistence.MailTplDO;
import com.iwindplus.integr.infrastructure.persistence.MailTplRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱模板业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_TPL})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailTplApplicationService {

    private final MailTplRepository mailTplRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean save(MailTplSaveDTO entity) {
        this.mailTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final MailTplDO model = BeanUtil.copyProperties(entity, MailTplDO.class);
        this.buildDefault(model);
        this.mailTplRepository.save(model);
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
        List<MailTplDO> list = this.mailTplRepository.listByIds(ids);
        if (Objects.isNull(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(MailTplDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.mailTplRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean edit(MailTplEditDTO entity) {
        // 编辑
        MailTplDO data = this.mailTplRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.mailTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.mailTplRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final MailTplDO model = BeanUtil.copyProperties(entity, MailTplDO.class);
        this.buildDefault(model);
        this.mailTplRepository.updateById(model);
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
        MailTplDO data = this.mailTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MailTplDO entity = new MailTplDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.mailTplRepository.updateById(entity);
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
        MailTplDO data = this.mailTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MailTplDO param = new MailTplDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.mailTplRepository.updateById(param);
        return Boolean.TRUE;
    }

    private void buildDefault(MailTplDO entity) {
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
