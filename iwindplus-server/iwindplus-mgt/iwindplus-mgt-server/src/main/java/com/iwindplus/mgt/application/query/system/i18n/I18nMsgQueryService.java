/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.i18n;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.system.i18n.dto.I18nMsgQueryDTO;
import com.iwindplus.mgt.application.query.system.i18n.dto.I18nMsgSearchDTO;
import com.iwindplus.mgt.application.query.system.i18n.vo.I18nMsgExtendVO;
import com.iwindplus.mgt.application.query.system.i18n.vo.I18nMsgPageVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.i18n.I18nMsgRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 国际化消息查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_I18N_MSG})
@Slf4j
@RequiredArgsConstructor
public class I18nMsgQueryService {

    private final I18nMsgRepository i18nMsgRepository;

    public IPage<I18nMsgPageVO> page(I18nMsgSearchDTO entity) {
        return this.i18nMsgRepository.selectPageByCondition(entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
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
