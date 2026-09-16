/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.i18n;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.service.system.i18n.dto.I18nMsgDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nMsgDO;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nMsgRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 国际化消息业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_I18N_MSG})
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class I18nMsgApplicationService {

    private final I18nMsgRepository i18nMsgRepository;

    @CacheEvict(allEntries = true)
    public boolean save(I18nMsgDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.i18nMsgRepository.getCodeIsExist(entity.getCode(), entity.getProjectId());
        entity.setSeq(this.i18nMsgRepository.getNextSeq(entity.getProjectId()));
        final I18nMsgDO model = BeanUtil.copyProperties(entity, I18nMsgDO.class);
        this.i18nMsgRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<I18nMsgDO> list = this.i18nMsgRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(I18nMsgDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.i18nMsgRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean edit(I18nMsgDTO entity) {
        I18nMsgDO data = this.i18nMsgRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.i18nMsgRepository.getCodeIsExist(entity.getCode().trim(), entity.getProjectId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final I18nMsgDO model = BeanUtil.copyProperties(entity, I18nMsgDO.class);
        this.i18nMsgRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        I18nMsgDO data = this.i18nMsgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        I18nMsgDO param = new I18nMsgDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.i18nMsgRepository.updateById(param);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        I18nMsgDO data = this.i18nMsgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        I18nMsgDO param = new I18nMsgDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.i18nMsgRepository.updateById(param);
        return Boolean.TRUE;
    }
}
