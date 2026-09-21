/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.MdcUtil;
import com.iwindplus.log.api.dto.MailLogDTO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.infrastructure.persistence.log.MailLogDO;
import com.iwindplus.log.infrastructure.persistence.log.MailLogRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱日志业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_LOG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailLogApplicationService {

    private final MailLogRepository mailLogRepository;

    /**
     * 添加.
     *
     * @param entity   对象
     * @return String
     */
    public String save(MailLogDTO entity) {
        final Map<String, String> headerMap = HeaderContextHolder.getContext();
        if (CharSequenceUtil.isBlank(entity.getRequestId())) {
            String requestId = Optional.ofNullable(headerMap)
                .map(map -> map.get(HeaderConstant.X_REQUESTED_ID))
                .filter(CharSequenceUtil::isNotBlank)
                .orElseGet(() -> MdcUtil.get(HeaderConstant.X_REQUESTED_ID));
            entity.setRequestId(requestId);
        }
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(IdUtil.simpleUUID());
        }
        entity.setTos(HtmlUtil.unescape(entity.getTos()));
        if (CharSequenceUtil.isNotBlank(entity.getCcs())) {
            entity.setCcs(HtmlUtil.unescape(entity.getCcs()));
        }
        if (CharSequenceUtil.isNotBlank(entity.getBccs())) {
            entity.setCcs(HtmlUtil.unescape(entity.getCcs()));
        }
        if (Objects.isNull(entity.getSendCount())) {
            entity.setSendCount(1);
        }
        if (Objects.isNull(entity.getResult())) {
            entity.setResult(Boolean.FALSE);
        }
        final MailLogDO model = BeanUtil.copyProperties(entity, MailLogDO.class);
        this.mailLogRepository.save(model);
        entity.setId(model.getId());
        return entity.getId();
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<String> ids) {
        List<MailLogDO> data = this.mailLogRepository.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.mailLogRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean edit(MailLogDTO entity) {
        MailLogDO data = this.mailLogRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getTos())) {
            entity.setTos(HtmlUtil.unescape(entity.getTos()));
        }
        if (CharSequenceUtil.isNotBlank(entity.getCcs())) {
            entity.setCcs(HtmlUtil.unescape(entity.getCcs()));
        }
        if (CharSequenceUtil.isNotBlank(entity.getBccs())) {
            entity.setCcs(HtmlUtil.unescape(entity.getCcs()));
        }
        final MailLogDO model = BeanUtil.copyProperties(entity, MailLogDO.class);
        this.mailLogRepository.updateById(model);
        return Boolean.TRUE;
    }
}
