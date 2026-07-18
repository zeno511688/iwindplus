/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.shiro.cache.ShiroRedisCacheManager;
import com.iwindplus.base.shiro.domain.constant.ShiroConstant;
import com.iwindplus.base.shiro.domain.property.ShiroProperty;
import com.iwindplus.base.shiro.domain.vo.AccessPermsVO;
import com.iwindplus.base.shiro.filter.CustomAuthenticationFilter;
import com.iwindplus.base.shiro.filter.CustomPermsAuthorizationFilter;
import com.iwindplus.base.shiro.filter.CustomRolesAuthorizationFilter;
import com.iwindplus.base.shiro.manager.ReloadPermissionManager;
import com.iwindplus.base.shiro.realm.ShiroRealm;
import com.iwindplus.base.shiro.service.ShiroService;
import com.iwindplus.base.web.support.WebManager;
import jakarta.annotation.Resource;
import jakarta.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 无状态配置.
 *
 * @author zengdegui
 * @since 2018/9/6
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ShiroProperty.class)
@ConditionalOnProperty(prefix = "shiro.jwt", name = "enabled", havingValue = "true")
public class ShiroConfiguration {

    @Lazy
    @Resource
    private ShiroService shiroService;

    @Resource
    private ShiroProperty property;

    @Resource
    private WebManager webManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 管理shiro bean生命周期.
     *
     * @return LifecycleBeanPostProcessor
     */
    @Bean("lifecycleBeanPostProcessor")
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        LifecycleBeanPostProcessor lifecycleBeanPostProcessor = new LifecycleBeanPostProcessor();
        log.info("LifecycleBeanPostProcessor={}", lifecycleBeanPostProcessor);
        return lifecycleBeanPostProcessor;
    }

    /**
     * 创建DefaultAdvisorAutoProxyCreator.
     *
     * @return DefaultAdvisorAutoProxyCreator
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        // 强制使用cglib
        creator.setProxyTargetClass(true);
        log.info("DefaultAdvisorAutoProxyCreator={}", creator);
        return creator;
    }

    /**
     * 开启shiro aop注解支持. 使用代理方式;所以需要开启代码支持, Controller才能使用@RequiresPermissions.
     *
     * @param securityManager 核心安全事务管理器
     * @return AuthorizationAttributeSourceAdvisor
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        log.info("AuthorizationAttributeSourceAdvisor={}", advisor);
        return advisor;
    }

    /**
     * ShiroFilterFactoryBean 处理拦截资源文件问题. 注意：单独一个ShiroFilterFactoryBean配置是或报错的，因为在 初始化ShiroFilterFactoryBean的时候需要注入：SecurityManager
     * <p>
     * Filter Chain定义说明 1、一个URL可以配置多个Filter，使用逗号分隔 2、当设置多个过滤器时，全部验证通过，才视为通过 3、部分过滤器可指定参数，如perms，roles
     *
     * @param securityManager 核心安全事务管理器
     * @return ShiroFilterFactoryBean
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>(16);
        // 配置访问权限，动态加载权限（从数据库读取然后配置）
        List<AccessPermsVO> entities = this.shiroService.listAccessPerms();
        if (CollUtil.isNotEmpty(entities)) {
            entities.stream().forEach(entity -> {
                String apiUrl = entity.getApiUrl();
                String permission = entity.getPermission();
                filterChainDefinitionMap.put(apiUrl, permission);
            });
        }
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        Map<String, Filter> filters = new LinkedHashMap<>(16);
        // 认证过滤器
        CustomAuthenticationFilter authenticationFilter = new CustomAuthenticationFilter(this.webManager);
        filters.put(this.property.getJwt().getAuthenticationFilterName(), authenticationFilter);
        // 角色过滤器
        CustomRolesAuthorizationFilter rolesAuthorizationFilter = new CustomRolesAuthorizationFilter();
        filters.put(this.property.getJwt().getRolesAuthorizationFilterName(), rolesAuthorizationFilter);
        // 权限过滤器
        CustomPermsAuthorizationFilter permsAuthorizationFilter = new CustomPermsAuthorizationFilter();
        filters.put(this.property.getJwt().getPermsAuthorizationFilterName(), permsAuthorizationFilter);
        shiroFilterFactoryBean.setFilters(filters);
        log.info("ShiroFilterFactoryBean={}", shiroFilterFactoryBean);
        return shiroFilterFactoryBean;
    }

    /**
     * 核心安全事务管理器.
     *
     * @param shiroRealm             shiroRealm
     * @param shiroRedisCacheManager shiroRedisCacheManager
     * @return SecurityManager
     */
    @Bean
    public SecurityManager securityManager(ShiroRealm shiroRealm, ShiroRedisCacheManager shiroRedisCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        List<Realm> realms = new LinkedList<>();
        realms.add(shiroRealm);
        // 设置Realm，用于获取认证凭证
        securityManager.setRealms(realms);
        //关闭shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        // 缓存管理器
        securityManager.setCacheManager(shiroRedisCacheManager);
        log.info("SecurityManager={}", securityManager);
        return securityManager;
    }

    /**
     * 无状态权限登录器.
     *
     * @param shiroRedisCacheManager shiroRedisCacheManager
     * @return ShiroRealm
     */
    @Bean
    public ShiroRealm shiroRealm(ShiroRedisCacheManager shiroRedisCacheManager) {
        ShiroRealm realm = new ShiroRealm();
        realm.setShiroService(this.shiroService);
        realm.setRedisTemplate(this.redisTemplate);
        realm.setShiroProperty(this.property);
        // 开启认证缓存
        realm.setAuthenticationCachingEnabled(true);
        realm.setAuthenticationCacheName(ShiroConstant.AUTHENTICATION_CACHE_NAME);
        // 开启授权缓存
        realm.setAuthorizationCachingEnabled(true);
        realm.setAuthorizationCacheName(ShiroConstant.AUTHORIZATION_CACHE_NAME);
        realm.setCacheManager(shiroRedisCacheManager);
        log.info("ShiroRealm={}", realm);
        return realm;
    }

    /**
     * 缓存管理器.
     *
     * @return ShiroRedisCacheManager
     */
    @Bean
    public ShiroRedisCacheManager shiroRedisCacheManager() {
        ShiroRedisCacheManager cacheManager = new ShiroRedisCacheManager();
        cacheManager.setRedisTemplate(this.redisTemplate);
        String prefix = new StringBuilder(ShiroConstant.REDIS_PREFIX).append(ShiroConstant.CACHE_KEY_PREFIX).toString();
        cacheManager.setKeyPrefix(prefix);
        // 单位:秒
        cacheManager.setTimeout(this.property.getJwt().getAccessTokenExpireTime());
        log.info("ShiroRedisCacheManager={}", cacheManager);
        return cacheManager;
    }

    /**
     * shiro 热加载权限.
     *
     * @param shiroFilterFactoryBean shiroFilterFactoryBean
     * @return ReloadPermissionManager
     */
    @Bean
    public ReloadPermissionManager reloadPermissionManager(ShiroFilterFactoryBean shiroFilterFactoryBean) {
        ReloadPermissionManager reloadPermissionManager = new ReloadPermissionManager();
        reloadPermissionManager.setShiroFilterFactoryBean(shiroFilterFactoryBean);
        reloadPermissionManager.setShiroService(this.shiroService);
        log.info("ReloadPermissionManager={}", reloadPermissionManager);
        return reloadPermissionManager;
    }

    /**
     * 添加ShiroDialect 为了在thymeleaf里使用shiro的标签的bean.
     *
     * @return ShiroDialect
     */
    @Bean
    public ShiroDialect shiroDialect() {
        ShiroDialect shiroDialect = new ShiroDialect();
        log.info("ShiroDialect={}", shiroDialect);
        return shiroDialect;
    }
}
