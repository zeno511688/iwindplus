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
import com.iwindplus.base.shiro.cache.ShiroRedisSessionDAO;
import com.iwindplus.base.shiro.domain.constant.ShiroConstant;
import com.iwindplus.base.shiro.domain.property.ShiroProperty;
import com.iwindplus.base.shiro.domain.vo.AccessPermsVO;
import com.iwindplus.base.shiro.filter.CustomFormAuthorizationFilter;
import com.iwindplus.base.shiro.filter.CustomPermsAuthorizationFilter;
import com.iwindplus.base.shiro.filter.CustomRolesAuthorizationFilter;
import com.iwindplus.base.shiro.manager.ReloadPermissionManager;
import com.iwindplus.base.shiro.manager.ShiroSessionManager;
import com.iwindplus.base.shiro.realm.ShiroSessionRealm;
import com.iwindplus.base.shiro.service.ShiroService;
import com.iwindplus.base.web.support.WebManager;
import jakarta.annotation.Resource;
import jakarta.servlet.Filter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.lang.codec.Base64;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 有状态配置.
 *
 * @author zengdegui
 * @since 2018/9/6
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ShiroProperty.class)
@ConditionalOnProperty(prefix = "shiro.session", name = "enabled", havingValue = "true")
public class ShiroSessionConfiguration {

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
        // 配置登录的url和登录成功的url
        shiroFilterFactoryBean.setLoginUrl(this.property.getSession().getLoginUrl());
        shiroFilterFactoryBean.setSuccessUrl(this.property.getSession().getSuccessUrl());
        shiroFilterFactoryBean.setUnauthorizedUrl(this.property.getSession().getUnauthorizedUrl());
        Map<String, Filter> filters = new LinkedHashMap<>(16);
        // 认证过滤器
        CustomFormAuthorizationFilter authorizationFilter = new CustomFormAuthorizationFilter(this.webManager);
        filters.put(this.property.getSession().getAuthenticationFilterName(), authorizationFilter);
        // 角色过滤器
        CustomRolesAuthorizationFilter rolesAuthorizationFilter = new CustomRolesAuthorizationFilter();
        filters.put(this.property.getSession().getRolesAuthorizationFilterName(), rolesAuthorizationFilter);
        // 权限过滤器
        CustomPermsAuthorizationFilter permsAuthorizationFilter = new CustomPermsAuthorizationFilter();
        filters.put(this.property.getSession().getPermsAuthorizationFilterName(), permsAuthorizationFilter);
        shiroFilterFactoryBean.setFilters(filters);
        log.info("ShiroFilterFactoryBean={}", shiroFilterFactoryBean);
        return shiroFilterFactoryBean;
    }

    /**
     * 核心安全事务管理器. s
     *
     * @param shiroSessionRealm      shiroSessionRealm
     * @param sessionManager         sessionManager
     * @param rememberMeManager      rememberMeManager
     * @param shiroRedisCacheManager shiroRedisCacheManager
     * @return SecurityManager
     */
    @Bean
    public SecurityManager securityManager(
        ShiroSessionRealm shiroSessionRealm, ShiroSessionManager sessionManager,
        RememberMeManager rememberMeManager, ShiroRedisCacheManager shiroRedisCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        List<Realm> realms = new LinkedList<>();
        realms.add(shiroSessionRealm);
        // 设置Realm，用于获取认证凭证
        securityManager.setRealms(realms);
        // session管理器
        securityManager.setSessionManager(sessionManager);
        // 记住我管理器
        securityManager.setRememberMeManager(rememberMeManager);
        // 缓存管理器
        securityManager.setCacheManager(shiroRedisCacheManager);
        log.info("SecurityManager={}", securityManager);
        return securityManager;
    }

    /**
     * 密码方式权限登录器.
     *
     * @param shiroRedisCacheManager shiroRedisCacheManager
     * @return shiroSessionRealm
     */
    @Bean
    public ShiroSessionRealm shiroSessionRealm(ShiroRedisCacheManager shiroRedisCacheManager) {
        ShiroSessionRealm realm = new ShiroSessionRealm();
        realm.setShiroService(this.shiroService);
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
     * 有状态记住我管理器.
     *
     * @param rememberMeCookie rememberMeCookie
     * @return CookieRememberMeManager
     */
    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie) {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie);
        // rememberMe cookie加密的密钥 建议每个项目都不一样
        // 默认AES算法 密钥长度(128 256 512 位)
        cookieRememberMeManager.setCipherKey(Base64.decode(this.property.getSession().getRememberCipherKey()));
        log.info("CookieRememberMeManager={}", cookieRememberMeManager);
        return cookieRememberMeManager;
    }

    /**
     * 有状态记住我cookie对象.
     *
     * @return SimpleCookie
     */
    @Bean
    public SimpleCookie rememberMeCookie() {
        // 这个参数是cookie的名称，对应前端的checkbox的name =
        // rememberMe
        SimpleCookie simpleCookie = new SimpleCookie(this.property.getSession().getRememberName());
        simpleCookie.setHttpOnly(true);
        simpleCookie.setSecure(true);
        // 记住我cookie生效时间30天 ,单位秒
        final BigDecimal bigDecimal = new BigDecimal(this.property.getSession().getRememberMeTimeout().toSeconds());
        simpleCookie.setMaxAge(bigDecimal.intValue());
        log.info("rememberMeCookie={}", simpleCookie);
        return simpleCookie;
    }

    /**
     * 有状态session管理器.
     *
     * @param shiroRedisSessionDAO                      shiroRedisSessionDAO
     * @param shiroRedisCacheManager                    shiroRedisCacheManager
     * @param sessionIdCookie                           sessionIdCookie
     * @param executorServiceSessionValidationScheduler executorServiceSessionValidationScheduler
     * @return ShiroSessionManager
     */
    @Bean
    public ShiroSessionManager sessionManager(
        ShiroRedisSessionDAO shiroRedisSessionDAO, ShiroRedisCacheManager shiroRedisCacheManager,
        SimpleCookie sessionIdCookie,
        ExecutorServiceSessionValidationScheduler executorServiceSessionValidationScheduler) {
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        // 去掉URL中的JSESSIONID
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        // 删除失效session
        sessionManager.setSessionValidationSchedulerEnabled(true);
        // 设置全局会话超时时间，单位:秒，默认30
        // 分钟，即如果30分钟内没有访问会话将过期
        sessionManager.setGlobalSessionTimeout(this.property.getSession().getCacheTimeout().toMillis());
        // 是否删除无效的，默认也是开启
        sessionManager.setDeleteInvalidSessions(true);
        // 是否开启 检测，默认开启
        sessionManager.setSessionValidationSchedulerEnabled(true);
        // 设置会话验证调度器
        sessionManager.setSessionValidationScheduler(executorServiceSessionValidationScheduler);
        // 注入sessionDao
        sessionManager.setSessionDAO(shiroRedisSessionDAO);
        sessionManager.setSessionIdCookie(sessionIdCookie);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setCacheManager(shiroRedisCacheManager);
        log.info("ShiroSessionManager={}", sessionManager);
        return sessionManager;
    }

    /**
     * 有状态定时清除无效的session.
     *
     * @return ExecutorServiceSessionValidationScheduler
     */
    @Bean
    public ExecutorServiceSessionValidationScheduler executorServiceSessionValidationScheduler() {
        ExecutorServiceSessionValidationScheduler scheduler = new ExecutorServiceSessionValidationScheduler();
        log.info("ExecutorServiceSessionValidationScheduler={}", scheduler);
        return scheduler;
    }

    /**
     * 有状态sessionId.
     *
     * @return SimpleCookie
     */
    @Bean
    public SimpleCookie sessionIdCookie() {
        SimpleCookie cookie = new SimpleCookie(this.property.getSession().getSessionIdCookieName());
        cookie.setHttpOnly(true);
        // cookie生效时间，单位:秒
        final BigDecimal bigDecimal = new BigDecimal(this.property.getSession().getCacheTimeout().toSeconds());
        cookie.setMaxAge(bigDecimal.intValue());
        log.info("sessionIdCookie={}", cookie);
        return cookie;
    }

    /**
     * 有状态session dao.
     *
     * @param shiroRedisCacheManager shiroRedisCacheManager
     * @return ShiroRedisSessionDAO
     */
    @Bean
    public ShiroRedisSessionDAO shiroRedisSessionDAO(ShiroRedisCacheManager shiroRedisCacheManager) {
        ShiroRedisSessionDAO sessionDAO = new ShiroRedisSessionDAO();
        sessionDAO.setRedisTemplate(this.redisTemplate);
        String prefix = new StringBuilder(ShiroConstant.REDIS_PREFIX).append(ShiroConstant.SESSION_KEY_PREFIX).toString();
        sessionDAO.setKeyPrefix(prefix);
        // 单位:秒
        sessionDAO.setTimeout(this.property.getSession().getCacheTimeout());
        sessionDAO.setCacheManager(shiroRedisCacheManager);
        sessionDAO.setActiveSessionsCacheName(ShiroConstant.ACTIVE_SESSION_CACHE_NAME);
        log.info("RedisSessionDAO={}", sessionDAO);
        return sessionDAO;
    }

    /**
     * 缓存管理器.
     *
     * @return shiroRedisCacheManager
     */
    @Bean
    public ShiroRedisCacheManager shiroRedisCacheManager() {
        ShiroRedisCacheManager cacheManager = new ShiroRedisCacheManager();
        cacheManager.setRedisTemplate(this.redisTemplate);
        String prefix = new StringBuilder(ShiroConstant.REDIS_PREFIX).append(ShiroConstant.CACHE_KEY_PREFIX).toString();
        cacheManager.setKeyPrefix(prefix);
        // 单位:秒
        cacheManager.setTimeout(this.property.getSession().getCacheTimeout());
        log.info("shiroRedisCacheManager={}", cacheManager);
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
