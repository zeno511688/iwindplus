package com.iwindplus.setup.server.service.impl;

import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BaseEnum;
import com.iwindplus.base.domain.enums.UserSexEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.wechat.domain.constant.WechatConstant;
import com.iwindplus.base.wechat.domain.vo.WechatMaPhoneNumberVO;
import com.iwindplus.base.wechat.domain.vo.WechatMaUserInfoVO;
import com.iwindplus.base.wechat.service.WechatMaService;
import com.iwindplus.base.wechat.service.WechatMpService;
import com.iwindplus.mgt.client.system.ThirdBindGrantClient;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSaveEditDTO;
import com.iwindplus.mgt.domain.enums.BindTypeEnum;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import com.iwindplus.setup.domain.dto.WechatMaGetQrCodeDTO;
import com.iwindplus.setup.domain.vo.WechatConfigMaVO;
import com.iwindplus.setup.domain.vo.WechatConfigMpVO;
import com.iwindplus.setup.server.service.WechatConfigMaService;
import com.iwindplus.setup.server.service.WechatConfigMpService;
import com.iwindplus.setup.server.service.WechatService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2021/9/2
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    private final WechatMaService wechatMaService;
    private final WechatMpService wechatMpService;
    private final WechatConfigMaService wechatConfigMaService;
    private final WechatConfigMpService wechatConfigMpService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ThirdBindGrantClient thirdBindGrantClient;

    @Override
    public String getWechatMpQrCode(String state) {
        final WechatConfigMpVO result = this.getWechatMpConfig(state);
        return this.wechatMpService.buildQrConnectUrl(result.getNotifyUrl(), WxConsts.QrConnectScope.SNSAPI_LOGIN, state);
    }

    @Override
    public void getWechatMpQrCodeCallback(String code, String state, HttpServletResponse response) {
        final WechatConfigMpVO result = this.getWechatMpConfig(state);
        WxOAuth2UserInfo data = this.wechatMpService.getUserInfo(code, null);
        log.info("WxOAuth2UserInfo={}", data);
        ThirdBindGrantSaveEditDTO param = new ThirdBindGrantSaveEditDTO();
        param.setOpenid(data.getOpenid());
        param.setUnionId(data.getUnionId());
        param.setType(BindTypeEnum.MP);
        ThirdBindGrantResultVO body = Optional.ofNullable(this.thirdBindGrantClient.saveOrEdit(param)).map(ResultVO::getBizData).orElse(null);
        final UrlBuilder urlBuilder = this.getUrlBuilder(result.getNotifySuccessUrl(), body, data.getNickname());
        if (Objects.nonNull(data.getSex())) {
            urlBuilder.addQuery(WechatConstant.SEX, BaseEnum.fromValue(data.getSex(), UserSexEnum.class));
        }
        this.buildRedirect(response, urlBuilder, data.getHeadImgUrl(), data.getCountry(), data.getProvince(), data.getCity());
    }

    @Override
    public ThirdBindGrantResultVO getWechatMaCodeByMobile(String code, String state) {
        WechatConfigMaVO result = this.getWechatMaConfig(state);
        WechatMaPhoneNumberVO data = this.wechatMaService.getPhoneNumberInfo(code);
        log.info("WechatMaPhoneNumberVO={}", data);
        ThirdBindGrantSaveEditDTO param = new ThirdBindGrantSaveEditDTO();
        param.setOpenid(data.getOpenid());
        param.setUnionId(data.getUnionId());
        param.setType(BindTypeEnum.MA);
        param.setMobile(data.getPurePhoneNumber());
        return Optional.ofNullable(this.thirdBindGrantClient.saveOrEdit(param)).map(ResultVO::getBizData).orElse(null);
    }

    @Override
    public void getWechatMaCodeByUserInfo(String code, String state, String rawData, String signature, String encryptedData, String iv,
        HttpServletResponse response) {
        WechatConfigMaVO result = this.getWechatMaConfig(state);
        WechatMaUserInfoVO data = this.wechatMaService.getUserInfo(code, rawData, signature, encryptedData, iv);
        log.info("WechatMaUserInfoVO={}", data);
        ThirdBindGrantSaveEditDTO param = new ThirdBindGrantSaveEditDTO();
        param.setOpenid(data.getOpenid());
        param.setUnionId(data.getUnionId());
        param.setType(BindTypeEnum.MA);
        ThirdBindGrantResultVO body = Optional.ofNullable(this.thirdBindGrantClient.saveOrEdit(param)).map(ResultVO::getBizData).orElse(null);
        final UrlBuilder urlBuilder = this.getUrlBuilder(result.getNotifySuccessUrl(), body, data.getNickName());
        if (CharSequenceUtil.isNotBlank(data.getGender())) {
            urlBuilder.addQuery(WechatConstant.SEX, BaseEnum.fromValue(Integer.valueOf(data.getGender()), UserSexEnum.class));
        }
        this.buildRedirect(response, urlBuilder, data.getAvatarUrl(), data.getCountry(), data.getProvince(), data.getCity());
    }

    @Override
    public String getWechatMaQrCode(WechatMaGetQrCodeDTO entity) {
        this.getWechatMaConfig(entity.getAccessKey());
        return this.wechatMaService.getQrCode(entity.getScene(), entity.getPage(), entity.getCheckPath(), entity.getEnvVersion(), entity.getWidth(),
            entity.getIsHyaline());
    }

    private WechatConfigMaVO getWechatMaConfig(String code) {
        WechatConfigMaVO result = this.wechatConfigMaService.getByCode(code);
        RedisTemplateWxRedisOps wxRedisOps = new RedisTemplateWxRedisOps(this.stringRedisTemplate);
        WxMaRedisBetterConfigImpl config = new WxMaRedisBetterConfigImpl(wxRedisOps, WechatConstant.WECHAT_MA_PREFIX);
        config.setAppid(result.getAccessKey());
        config.setSecret(result.getSecretKey());
        config.setToken(result.getToken());
        config.setAesKey(result.getAesKey());
        config.setMsgDataFormat(result.getMsgDataFormat());
        this.wechatMaService.addConfig(result.getAccessKey(), config);
        return result;
    }

    private WechatConfigMpVO getWechatMpConfig(String code) {
        WechatConfigMpVO result = this.wechatConfigMpService.getByCode(code);
        RedisTemplateWxRedisOps wxRedisOps = new RedisTemplateWxRedisOps(this.stringRedisTemplate);
        final WxMpRedisConfigImpl config = new WxMpRedisConfigImpl(wxRedisOps, WechatConstant.WECHAT_MP_PREFIX);
        config.setAppId(result.getAccessKey());
        config.setSecret(result.getSecretKey());
        config.setToken(result.getToken());
        config.setAesKey(result.getAesKey());
        this.wechatMpService.addConfigStorage(result.getAccessKey(), config);
        return result;
    }

    private UrlBuilder getUrlBuilder(String url, ThirdBindGrantResultVO body, String nickName) {
        final UrlBuilder urlBuilder = UrlBuilder.ofHttp(url)
            .addQuery(WechatConstant.CODE, body.getCode())
            .addQuery(WechatConstant.BIND_FLAG, body.getBindFlag());
        if (CharSequenceUtil.isNotBlank(nickName)) {
            urlBuilder.addQuery(WechatConstant.NICK_NAME, nickName);
        }
        return urlBuilder;
    }

    private void buildRedirect(HttpServletResponse response, UrlBuilder urlBuilder, String avatar, String country, String province, String city) {
        if (CharSequenceUtil.isNotBlank(avatar)) {
            urlBuilder.addQuery(WechatConstant.AVATAR, avatar);
        }
        if (CharSequenceUtil.isNotBlank(country)) {
            urlBuilder.addQuery(WechatConstant.COUNTRY, country);
        }
        if (CharSequenceUtil.isNotBlank(province)) {
            urlBuilder.addQuery(WechatConstant.PROVINCE, province);
        }
        if (CharSequenceUtil.isNotBlank(city)) {
            urlBuilder.addQuery(WechatConstant.CITY, city);
        }
        String redirectUrl = urlBuilder.build();
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
    }
}
