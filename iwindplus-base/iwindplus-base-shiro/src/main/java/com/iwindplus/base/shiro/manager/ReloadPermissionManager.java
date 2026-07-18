/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.manager;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.shiro.domain.vo.AccessPermsVO;
import com.iwindplus.base.shiro.service.ShiroService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * shiro热加载权限.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReloadPermissionManager {
    private ShiroFilterFactoryBean shiroFilterFactoryBean;
    private ShiroService shiroService;

    /**
     * 重新加载权限.
     */
    public void updatePermission() {
        synchronized (this.shiroFilterFactoryBean) {
            AbstractShiroFilter shiroFilter;
            try {
                shiroFilter = (AbstractShiroFilter) this.shiroFilterFactoryBean.getObject();
                if (Objects.nonNull(shiroFilter)) {
                    reloadPermission(shiroFilter);
                }
            } catch (Exception ex) {
                log.error(ExceptionConstant.EXCEPTION, ex);
            }
        }
    }

    private void reloadPermission(AbstractShiroFilter shiroFilter) {
        PathMatchingFilterChainResolver filterChainResolver = (PathMatchingFilterChainResolver) shiroFilter
                .getFilterChainResolver();
        DefaultFilterChainManager manager = (DefaultFilterChainManager) filterChainResolver
                .getFilterChainManager();
        // 清空老的权限控制.
        manager.getFilterChains().clear();
        this.shiroFilterFactoryBean.getFilterChainDefinitionMap().clear();
        // 配置访问权限.
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>(16);
        // 配置访问权限，动态加载权限（从数据库读取然后配置）.
        List<AccessPermsVO> entities = this.shiroService.listAccessPerms();
        if (!CollUtil.isEmpty(entities)) {
            entities.stream().forEach(entity -> {
                String apiUrl = entity.getApiUrl();
                String permission = entity.getPermission();
                filterChainDefinitionMap.put(apiUrl, permission);
            });
        }
        this.shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        // 重新构建生成.
        Map<String, String> chains = this.shiroFilterFactoryBean.getFilterChainDefinitionMap();
        // 重新生成过滤链.
        if (MapUtils.isNotEmpty(chains)) {
            chains.entrySet().stream().forEach(chain -> manager.createChain(chain.getKey(), chain.getValue().replace(" ", "")));
        }
        log.info("update shiro permission！！");
    }
}