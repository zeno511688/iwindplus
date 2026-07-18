/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat;

import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.wechat.domain.constant.WechatConstant;
import com.iwindplus.base.wechat.domain.property.WechatProperty;
import com.iwindplus.base.wechat.service.impl.WechatMpServiceImpl;
import com.iwindplus.base.wechat.service.WechatMaService;
import com.iwindplus.base.wechat.service.WechatMpService;
import com.iwindplus.base.wechat.service.WechatPayService;
import com.iwindplus.base.wechat.service.impl.WechatMaServiceImpl;
import com.iwindplus.base.wechat.service.impl.WechatPayServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;

/**
 * 微信小程序配置管理.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WechatProperty.class)
public class WechatConfiguration {
    @Resource
    private WechatProperty property;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建 WechatMaService.
     *
     * @return WechatMaService
     */
    @ConditionalOnProperty(prefix = "wechat.ma", name = "enabled", havingValue = "true")
    @Bean
    public WechatMaService wechatMaService() {
        WechatMaService maService = new WechatMaServiceImpl();
        WechatProperty.MaConfig ma = this.property.getMa();
        if (Objects.nonNull(ma) && CharSequenceUtil.isNotBlank(ma.getAppId()) && CharSequenceUtil.isNotBlank(ma.getSecret())) {
            WxMaDefaultConfigImpl config;
            if (Boolean.TRUE.equals(ma.getUseRedis())) {
                RedisTemplateWxRedisOps wxRedisOps = new RedisTemplateWxRedisOps(this.stringRedisTemplate);
                config = new WxMaRedisBetterConfigImpl(wxRedisOps, WechatConstant.WECHAT_MA_PREFIX);
            } else {
                config = new WxMaDefaultConfigImpl();
            }
            config.setAppid(ma.getAppId());
            config.setSecret(ma.getSecret());
            config.setToken(ma.getToken());
            config.setAesKey(ma.getAesKey());
            config.setMsgDataFormat(ma.getMsgDataFormat());
            maService.setWxMaConfig(config);
        }
        log.info("WechatMaService={}", maService);
        return maService;
    }

    /**
     * 创建 WechatMpService.
     *
     * @return WechatMpService
     */
    @ConditionalOnProperty(prefix = "wechat.mp", name = "enabled", havingValue = "true")
    @Bean
    public WechatMpService wechatMpService() {
        WechatMpService mpService = new WechatMpServiceImpl();
        WechatProperty.MpConfig mp = this.property.getMp();
        if (Objects.nonNull(mp) && CharSequenceUtil.isNotBlank(mp.getAppId()) && CharSequenceUtil.isNotBlank(mp.getSecret())) {
            WxMpDefaultConfigImpl config;
            if (Boolean.TRUE.equals(mp.getUseRedis())) {
                RedisTemplateWxRedisOps wxRedisOps = new RedisTemplateWxRedisOps(this.stringRedisTemplate);
                config = new WxMpRedisConfigImpl(wxRedisOps, WechatConstant.WECHAT_MP_PREFIX);
            } else {
                config = new WxMpDefaultConfigImpl();
            }
            config.setAppId(mp.getAppId());
            config.setSecret(mp.getSecret());
            config.setToken(mp.getToken());
            config.setAesKey(mp.getAesKey());
            mpService.setWxMpConfigStorage(config);
        }
        log.info("WechatMpService={}", mpService);
        return mpService;
    }

    /**
     * 创建 WechatPayService.
     *
     * @return WechatPayService
     */
    @ConditionalOnProperty(prefix = "wechat.pay", name = "enabled", havingValue = "true")
    @Bean
    public WechatPayService wechatPayService() {
        WechatPayService wechatPayService = new WechatPayServiceImpl();
        WechatProperty.PayConfig pay = this.property.getPay();
        wechatPayService.setConfig(pay);
        log.info("WechatPayService={}", wechatPayService);
        return wechatPayService;
    }
}