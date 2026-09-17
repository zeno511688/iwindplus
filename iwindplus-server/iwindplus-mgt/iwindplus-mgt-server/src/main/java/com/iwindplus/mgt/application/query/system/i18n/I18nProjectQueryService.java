/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.i18n;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseVO;
import com.iwindplus.mgt.application.query.system.i18n.dto.I18nMsgQueryDTO;
import com.iwindplus.mgt.application.query.system.i18n.dto.I18nProjectSearchDTO;
import com.iwindplus.mgt.application.query.system.i18n.vo.I18nMsgExtendVO;
import com.iwindplus.mgt.application.query.system.i18n.vo.I18nProjectExtendVO;
import com.iwindplus.mgt.application.query.system.i18n.vo.I18nProjectPageVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nMsgRepository;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nProjectDO;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nProjectRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 国际化项目查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_I18N_PROJECT})
@Slf4j
@RequiredArgsConstructor
public class I18nProjectQueryService {

    private final I18nProjectRepository i18nProjectRepository;
    private final I18nMsgRepository i18nMsgRepository;

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<I18nProjectPageVO> page(I18nProjectSearchDTO entity) {
        return this.i18nProjectRepository.page(entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public I18nProjectExtendVO getDetail(Long id) {
        final I18nProjectDO data = this.i18nProjectRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final I18nProjectExtendVO result = BeanUtil.copyProperties(data, I18nProjectExtendVO.class);
        final I18nMsgQueryDTO queryDTO = I18nMsgQueryDTO
            .builder()
            .projectId(id)
            .build();
        final List<I18nMsgExtendVO> list = this.i18nMsgRepository.listByCondition(queryDTO);
        if (CollUtil.isNotEmpty(list)) {
            result.setContent(this.i18nMsgRepository.buildContent(list));
        }
        return result;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<BaseVO> listEnabled() {
        LambdaQueryWrapper<I18nProjectDO> queryWrapper = Wrappers.lambdaQuery(I18nProjectDO.class)
            .eq(I18nProjectDO::getStatus, EnableStatusEnum.ENABLE)
            .select(I18nProjectDO::getId, I18nProjectDO::getCode, I18nProjectDO::getName)
            .orderByAsc(List.of(I18nProjectDO::getSeq));
        List<I18nProjectDO> list = this.i18nProjectRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, BaseVO.class);
    }
}
