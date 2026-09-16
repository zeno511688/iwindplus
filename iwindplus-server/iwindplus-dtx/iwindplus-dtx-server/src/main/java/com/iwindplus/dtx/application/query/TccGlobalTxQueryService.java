/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.application.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.dtx.application.query.vo.TccGlobalTxPageVO;
import com.iwindplus.dtx.application.query.vo.TccGlobalTxVO;
import com.iwindplus.dtx.application.query.dto.TccGlobalTxSearchDTO;
import com.iwindplus.dtx.application.query.dto.TccGlobalTxShardSearchDTO;
import com.iwindplus.dtx.infrastructure.persistence.TccGlobalTxDO;
import com.iwindplus.dtx.infrastructure.persistence.TccGlobalTxRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * tcc全局事务查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TccGlobalTxQueryService {

    private final TccGlobalTxRepository tccGlobalTxRepository;

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<TccGlobalTxPageVO>
     */
    public IPage<TccGlobalTxPageVO> page(TccGlobalTxSearchDTO entity) {
        PageDTO<TccGlobalTxDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<TccGlobalTxDO> queryWrapper = Wrappers.lambdaQuery(TccGlobalTxDO.class)
            .orderByAsc(TccGlobalTxDO::getId);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(TccGlobalTxDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getXid())) {
            queryWrapper.eq(TccGlobalTxDO::getXid, entity.getXid().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizType())) {
            queryWrapper.eq(TccGlobalTxDO::getBizType, entity.getBizType().trim());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(TccGlobalTxDO::getStatus, entity.getStatusList());
        }
        final PageDTO<TccGlobalTxDO> modelPage = this.tccGlobalTxRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, TccGlobalTxPageVO.class));
    }

    /**
     * 详情.
     *
     * @param xid 全局事务ID
     * @return TccGlobalTxVO
     */
    public TccGlobalTxVO getDetailByXid(String xid) {
        TccGlobalTxDO data = this.tccGlobalTxRepository.getOne(Wrappers.lambdaQuery(TccGlobalTxDO.class)
            .eq(TccGlobalTxDO::getXid, xid));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, TccGlobalTxVO.class);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return TccGlobalTxVO
     */
    public TccGlobalTxVO getDetail(Long id) {
        TccGlobalTxDO data = this.tccGlobalTxRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, TccGlobalTxVO.class);
    }

    /**
     * 分片查询.
     *
     * @param entity 搜索条件
     * @return List<TccGlobalTxVO>
     */
    public List<TccGlobalTxVO> listByShard(TccGlobalTxShardSearchDTO entity) {
        LambdaQueryWrapper<TccGlobalTxDO> queryWrapper = Wrappers.lambdaQuery(TccGlobalTxDO.class)
            .eq(TccGlobalTxDO::getEnv, SpringUtil.getActiveProfile())
            .gt(TccGlobalTxDO::getId, Objects.isNull(entity.getLastId()) ? 0L : entity.getLastId())
            .orderByAsc(TccGlobalTxDO::getId)
            .last("LIMIT " + entity.getSize());

        final Integer shardTotal = entity.getShardTotal();
        if (Objects.nonNull(shardTotal) && shardTotal > 1) {
            final int shardIndex = Objects.isNull(entity.getShardIndex()) ? 0 : entity.getShardIndex();
            queryWrapper.apply("MOD(id, {0}) = {1}", shardTotal, shardIndex);
        }
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(TccGlobalTxDO::getStatus, entity.getStatus());
        }
        if (CollUtil.isNotEmpty(entity.getStatusList())) {
            queryWrapper.in(TccGlobalTxDO::getStatus, entity.getStatusList());
        }
        if (Objects.nonNull(entity.getExpireTime())) {
            queryWrapper.lt(TccGlobalTxDO::getExpireTime, entity.getExpireTime());
        }
        if (Objects.nonNull(entity.getRetryTime())) {
            queryWrapper.le(TccGlobalTxDO::getNextRetryTime, entity.getRetryTime());
        }
        if (Objects.nonNull(entity.getRetryCount())) {
            queryWrapper.le(TccGlobalTxDO::getRetryCount, entity.getRetryCount());
        }

        final List<TccGlobalTxDO> list = this.tccGlobalTxRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }

        return BeanUtil.copyToList(list, TccGlobalTxVO.class);
    }

}
