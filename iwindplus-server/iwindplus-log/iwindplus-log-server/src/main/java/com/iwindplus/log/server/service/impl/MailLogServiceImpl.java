/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.log.domain.dto.MailLogDTO;
import com.iwindplus.log.domain.dto.MailLogSearchDTO;
import com.iwindplus.log.domain.vo.MailLogPageVO;
import com.iwindplus.log.domain.vo.MailLogVO;
import com.iwindplus.log.server.dal.model.MailLogDO;
import com.iwindplus.log.server.service.MailLogService;
import com.iwindplus.mgt.client.power.UserClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱日志业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {"mailLog"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailLogServiceImpl extends EsBaseServiceImpl<MailLogDO>
    implements MailLogService {

    private final UserClient userClient;

    @Override
    public String save(MailLogDTO entity) {
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
        super.save(model);
        entity.setId(model.getId());
        return entity.getId();
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<String> ids) {
        List<MailLogDO> data = super.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(MailLogDTO entity) {
        MailLogDO data = super.getById(entity.getId());
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
        super.updateById(model);
        return Boolean.TRUE;
    }

    @Override
    public IPage<MailLogPageVO> page(MailLogSearchDTO entity) {
        final EsLambdaQueryWrapper<MailLogDO> wrapper = new EsLambdaQueryWrapper<>();
        final PageDTO<MailLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(MailLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(MailLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank((entity.getSubject()))) {
            wrapper.eq(MailLogDO::getSubject,entity.getSubject());
        }
        if (CharSequenceUtil.isNotBlank((entity.getContent()))) {
            wrapper.like(MailLogDO::getContent, entity.getContent());
        }
        if (CharSequenceUtil.isNotBlank((entity.getNickName()))) {
            wrapper.eq(MailLogDO::getNickName, entity.getNickName());
        }
        if (CharSequenceUtil.isNotBlank((entity.getUsername()))) {
            wrapper.eq(MailLogDO::getUsername, entity.getUsername());
        }
        if (CharSequenceUtil.isNotBlank((entity.getTos()))) {
            wrapper.like(MailLogDO::getTos, entity.getTos());
        }
        if (Objects.nonNull(entity.getResult())) {
            wrapper.eq(MailLogDO::getResult, entity.getResult());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(MailLogDO::getUserId, entity.getUserId());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<MailLogDO> modelPage = super.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailLogPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MailLogVO getDetail(String id) {
        MailLogDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, MailLogVO.class);
    }
}
