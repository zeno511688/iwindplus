package com.iwindplus.auth.server.service.impl;

import com.iwindplus.auth.server.service.BindCodeDetailsService;
import com.iwindplus.auth.server.service.SysUserDetailsService;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserDetailVO;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 第三方绑定授权认证业务层接口实现类.
 *
 * @author zengdegui 2024/12/4 11:05
 */
@Service
public class BindCodeDetailsServiceImpl implements BindCodeDetailsService {

    @Resource
    private UserClient userClient;

    @Override
    public UserDetails loadUserByCode(String code) {
        ResultVO<UserDetailVO> result = this.userClient.getLoginByCode(code);
        result.errorThrow();
        final UserDetailVO data = result.getBizData();
        return SysUserDetailsService.getUserDetails(data);
    }
}
