package com.iwindplus.auth.server.service.impl;

import com.iwindplus.auth.server.service.SmsCodeDetailsService;
import com.iwindplus.auth.server.service.SysUserDetailsService;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.mgt.client.power.UserClient;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 短信认证业务层接口实现类.
 *
 * @author zengdegui 2024/12/4 11:05
 */
@Service
public class SmsCodeDetailsServiceImpl implements SmsCodeDetailsService {
    @Resource
    private UserClient userClient;

    @Resource
    private MailCaptchaLogClient mailCaptchaLogClient;

    /**
     * 手机号码认证方式.
     *
     * @param mobile 手机号
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByMobile(String mobile) {
        return SysUserDetailsService.getUserDetails(userClient, mobile);
    }

    /**
     * 验证手机验证码是否正确.
     *
     * @param code    配置编码
     * @param mobile  手机
     * @param captcha 验证码
     * @return boolean
     */
    @Override
    public boolean validate(String code, String mobile, String captcha) {
        ResultVO<Boolean> result = this.mailCaptchaLogClient.validate(code, mobile, captcha);
        result.errorThrow();
        return result.getBizData();
    }
}
