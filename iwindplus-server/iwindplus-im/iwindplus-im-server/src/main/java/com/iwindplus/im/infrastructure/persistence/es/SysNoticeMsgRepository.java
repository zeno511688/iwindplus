/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.persistence.es;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import java.util.List;
import java.util.Objects;
import org.springframework.data.elasticsearch.client.elc.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

/**
 * 系统通知消息查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:50
 */
@Repository
public class SysNoticeMsgRepository extends EsBaseServiceImpl<SysNoticeMsgDO> implements EsBaseService<SysNoticeMsgDO> {

    /**
     * 查询下一个排序号.
     *
     * @param orgId 组织主键
     * @return Integer
     */
    public Integer getNextSeq(Long orgId) {
        EsLambdaQueryWrapper<SysNoticeMsgDO> wrapper = EsWrappers.<SysNoticeMsgDO>lambdaQuery()
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
}
