/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.persistence.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.im.application.query.vo.DirectMsgVO;
import com.iwindplus.im.common.enums.SendStatusEnum;
import java.util.List;
import java.util.Objects;
import org.springframework.data.elasticsearch.client.elc.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

/**
 * 直发消息查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:50
 */
@Repository
public class DirectMsgRepository extends EsBaseServiceImpl<DirectMsgDO> implements EsBaseService<DirectMsgDO> {

    /**
     * 查询未接收到的.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return List<DirectMsgVO>
     */
    public List<DirectMsgVO> listByUnSendSuccess(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<DirectMsgDO> wrapper = EsWrappers.<DirectMsgDO>lambdaQuery()
            .eq(DirectMsgDO::getOrgId, orgId)
            .eq(DirectMsgDO::getReceiverId, userId)
            .ne(DirectMsgDO::getSendStatus, SendStatusEnum.SUCCESS);
        final List<DirectMsgDO> list = super.list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, DirectMsgVO.class);
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId    组织主键
     * @param senderId 发送人主键
     * @return Integer
     */
    public Integer getNextSeq(Long orgId, Long senderId) {
        EsLambdaQueryWrapper<DirectMsgDO> wrapper = EsWrappers.<DirectMsgDO>lambdaQuery()
            .eq(DirectMsgDO::getOrgId, orgId)
            .eq(DirectMsgDO::getSenderId, senderId)
            .max(DirectMsgDO::getSeq)
            .limit(0);

        SearchHits<DirectMsgDO> result =
            super.getOperations().search(
                wrapper.build(),
                DirectMsgDO.class
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
}
