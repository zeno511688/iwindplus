/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.im.domain.dto.SysNoticeMsgDTO;
import com.iwindplus.im.domain.dto.SysNoticeMsgSearchDTO;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.SysNoticeMsgPageVO;
import com.iwindplus.im.domain.vo.SysNoticeMsgVO;
import com.iwindplus.im.server.dal.model.SysNoticeMsgDO;
import com.iwindplus.im.server.service.SysNoticeMsgService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.setup.client.OssClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统通知消息业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SysNoticeMsgServiceImpl extends EsBaseServiceImpl<SysNoticeMsgDO> implements SysNoticeMsgService {

    private final OssClient ossClient;
    private final UserClient userClient;

    @Override
    public boolean save(SysNoticeMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        List<Long> ids = Arrays.asList(userId);
        final UserExtendVO data = Optional.ofNullable(this.userClient.listExtendByIds(ids)).map(ResultVO::getBizData).map(m -> m.get(0))
            .orElse(null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        String avatar = data.getAvatar();
        String nickName = data.getNickName();
        entity.setSeq(this.getNextSeq(orgId));
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);
        entity.setOrgId(orgId);
        final SysNoticeMsgDO model = BeanUtil.copyProperties(entity, SysNoticeMsgDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(SysNoticeMsgDTO entity) {
        SysNoticeMsgDO data = super.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final SysNoticeMsgDO model = BeanUtil.copyProperties(entity, SysNoticeMsgDO.class);
        return super.updateById(model);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        List<SysNoticeMsgDO> list = super.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @Override
    public IPage<SysNoticeMsgPageVO> page(PageDTO<SysNoticeMsgDO> page, SysNoticeMsgSearchDTO entity) {
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSenderId())) {
            entity.setSenderId(userId);
        }
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<SysNoticeMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(SysNoticeMsgDO::getOrgId, orgId)
            .eq(SysNoticeMsgDO::getSenderId, entity.getSenderId())
            .eq(SysNoticeMsgDO::getSendStatus, entity.getSendStatus());
        if (CharSequenceUtil.isNotBlank(entity.getTitle())) {
            wrapper.like(SysNoticeMsgDO::getTitle, entity.getTitle());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<SysNoticeMsgDO> modelPage = super.page(page, wrapper);
        final IPage<SysNoticeMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, SysNoticeMsgPageVO.class));
        return result;
    }

    @Override
    public SysNoticeMsgVO getDetail(String id, String ossTplCode) {
        SysNoticeMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getSenderAvatar())) {
            relativePaths.add(data.getSenderAvatar());
        }
        List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
        final SysNoticeMsgVO result = BeanUtil.copyProperties(data, SysNoticeMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId 组织主键
     * @return Integer
     */
    private Integer getNextSeq(Long orgId) {
        EsLambdaQueryWrapper<SysNoticeMsgDO> wrapper = new EsLambdaQueryWrapper<>();

        wrapper
            .eq(SysNoticeMsgDO::getOrgId, orgId)
            .max(SysNoticeMsgDO::getSeq)
            .limit(0);

        SearchHits<SysNoticeMsgDO> result =
            getOperations().search(
                wrapper.build(),
                SysNoticeMsgDO.class
            );

        return parseSeq(result.getAggregations().aggregations());
    }

    private int parseSeq(Object aggregations) {
        if (aggregations instanceof List<?> aggregationList) {
            return aggregationList.stream()
                .filter(ElasticsearchAggregation.class::isInstance)
                .map(ElasticsearchAggregation.class::cast)
                .map(ElasticsearchAggregation::aggregation)
                .map(Aggregation::getAggregate)
                .map(Aggregate::max)
                .map(max -> max != null ? max.value() : null)
                .filter(Objects::nonNull)
                .findFirst()
                .map(i -> i.intValue() + 1)
                .orElse(1);
        }
        return 1;
    }

    private void buildUserInfo(SysNoticeMsgVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
        });
    }
}