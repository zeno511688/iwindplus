/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */


package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import com.iwindplus.log.domain.dto.BinlogAlertSearchDTO;
import com.iwindplus.log.domain.vo.BinlogAlertPageVO;
import com.iwindplus.log.domain.vo.BinlogAlertVO;
import com.iwindplus.log.server.dal.model.BinlogAlertDO;
import com.iwindplus.log.server.service.BinlogAlertService;
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
 * binlog告警业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */

@Slf4j
@Service
@CacheConfig(cacheNames = {"binlogAlert"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BinlogAlertServiceImpl extends EsBaseServiceImpl<BinlogAlertDO>
    implements BinlogAlertService {

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(BinlogAlertDTO entity) {
        final BinlogAlertDO model = BeanUtil.copyProperties(entity, BinlogAlertDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean saveBatch(List<BinlogAlertDTO> entities) {
        final List<BinlogAlertDO> models = BeanUtil.copyToList(entities, BinlogAlertDO.class);
        super.saveBatch(models);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<String> ids) {
        List<BinlogAlertDO> data = super.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return super.removeByIds(ids, false);
    }

    @Override
    public IPage<BinlogAlertPageVO> page(BinlogAlertSearchDTO entity) {
        final EsLambdaQueryWrapper<BinlogAlertDO> wrapper = new EsLambdaQueryWrapper<>();
        final PageDTO<BinlogAlertDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        if (Objects.nonNull(entity.getDataId())) {
            wrapper.eq(BinlogAlertDO::getDataId, entity.getDataId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getDb())) {
            wrapper.eq(BinlogAlertDO::getDb, entity.getDb());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTable())) {
            wrapper.eq(BinlogAlertDO::getTable, entity.getTable());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<BinlogAlertDO> modelPage = super.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, BinlogAlertPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public BinlogAlertVO getDetail(String id) {
        BinlogAlertDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, BinlogAlertVO.class);
    }

}
