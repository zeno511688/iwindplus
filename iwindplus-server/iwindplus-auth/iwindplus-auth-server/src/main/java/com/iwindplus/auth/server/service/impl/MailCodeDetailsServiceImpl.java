package com.iwindplus.auth.server.service.impl;

import com.iwindplus.auth.server.service.MailCodeDetailsService;
import com.iwindplus.auth.server.service.SysUserDetailsService;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.client.MailCaptchaLogClient;
import com.iwindplus.mgt.client.power.UserClient;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 邮箱认证业务层接口实现类.
 *
 * @author zengdegui 2024/12/4 11:05
 */
@Service
public class MailCodeDetailsServiceImpl implements MailCodeDetailsService {

    @Resource
    private UserClient userClient;

    @Resource
    private MailCaptchaLogClient mailCaptchaLogClient;

    /**
     * 邮箱认证方式.
     *
     * @param mail 邮箱
     * @return UserDetails
     */
    @Override
    public UserDetails loadUserByMail(String mail) {
        return SysUserDetailsService.getUserDetails(userClient, mail);
    }

    /**
     * 验证邮箱验证码是否正确.
     *
     * @param code    编码
     * @param mail    邮箱
     * @param captcha 验证码
     * @return boolean
     */
    @Override
    public boolean validate(String code, String mail, String captcha) {
        ResultVO<Boolean> result = this.mailCaptchaLogClient.validate(code, mail, captcha);
        result.errorThrow();
        return result.getBizData();
    }
}
