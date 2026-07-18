package com.iwindplus.gateway.server.domain.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.MetadataConstant;
import com.iwindplus.mgt.domain.dto.system.ServerRouteParamDTO;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * 服务路由对象转换器.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class RouteDefinitionConverter {

    /**
     * 转换
     *
     * @param source 源
     * @return RouteDefinition
     */
    public static RouteDefinition convertToRouteDefinition(ServerRouteDefinitionVO source) {
        if (!isValidSource(source)) {
            return null;
        }

        RouteDefinition result = new RouteDefinition();
        result.setId(source.getId());
        result.setUri(URI.create(source.getUri()));
        result.setPredicates(RouteDefinitionConverter.buildPredicateDefinition(source.getPredicates()));
        result.setFilters(RouteDefinitionConverter.buildFilterDefinition(source.getFilters()));
        result.setOrder(source.getOrder());
        result.setMetadata(source.getMetadata());
        return result;
    }

    static List<PredicateDefinition> buildPredicateDefinition(List<ServerRouteParamDTO> predicateList) {
        if (CollUtil.isEmpty(predicateList)) {
            return null;
        }

        return predicateList.stream().filter(Objects::nonNull).map(m -> {
            PredicateDefinition param = new PredicateDefinition();
            param.setName(m.getName());
            param.setArgs(m.getArgs());
            return param;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    static List<FilterDefinition> buildFilterDefinition(List<ServerRouteParamDTO> filterList) {
        if (CollUtil.isEmpty(filterList)) {
            return null;
        }

        return filterList.stream().filter(Objects::nonNull).map(m -> {
            FilterDefinition param = new FilterDefinition();
            param.setName(m.getName());
            param.setArgs(m.getArgs());
            return param;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private static boolean isValidSource(ServerRouteDefinitionVO source) {
        String id = source.getId();
        String uri = source.getUri();
        List<ServerRouteParamDTO> predicateList = source.getPredicates();
        Map<String, Object> metadata = source.getMetadata();
        final boolean metadataFlag = MapUtil.isEmpty(metadata) || Objects.isNull(metadata.get(MetadataConstant.NAME));
        return CharSequenceUtil.isNotBlank(id)
            && CharSequenceUtil.isNotBlank(uri)
            && CollUtil.isNotEmpty(predicateList)
            && Boolean.FALSE.equals(metadataFlag);
    }
}
