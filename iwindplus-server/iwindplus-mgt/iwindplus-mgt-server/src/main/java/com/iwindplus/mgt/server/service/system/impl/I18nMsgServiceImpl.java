/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.I18nMsgDTO;
import com.iwindplus.mgt.domain.dto.system.I18nMsgQueryDTO;
import com.iwindplus.mgt.domain.dto.system.I18nMsgSearchDTO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgPageVO;
import com.iwindplus.mgt.server.dal.model.system.I18nMsgDO;
import com.iwindplus.mgt.server.dal.repository.system.I18nMsgRepository;
import com.iwindplus.mgt.server.service.system.I18nMsgService;
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
public class I18nMsgServiceImpl implements I18nMsgService {

    private final I18nMsgRepository i18nMsgRepository;

    @CacheEvict(allEntries = true)
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public IPage<I18nMsgPageVO> page(I18nMsgSearchDTO entity) {
        return this.i18nMsgRepository.selectPageByCondition(entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public I18nMsgExtendVO getDetail(Long id) {
        final I18nMsgQueryDTO queryDTO = I18nMsgQueryDTO
            .builder()
            .id(id)
            .build();
        final List<I18nMsgExtendVO> data = this.i18nMsgRepository.listByCondition(queryDTO);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return data.get(0);
    }
}
