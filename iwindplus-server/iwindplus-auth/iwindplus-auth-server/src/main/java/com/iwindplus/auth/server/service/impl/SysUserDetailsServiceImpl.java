/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service.impl;

import com.iwindplus.auth.server.service.SysUserDetailsService;
import com.iwindplus.mgt.client.power.UserClient;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 系统用户信息加载实现类.
 *
 * @author zengdegui
 * @since 2024/06/11 20:38
 */
@Service
public class SysUserDetailsServiceImpl implements SysUserDetailsService {
    @Resource
    private UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return SysUserDetailsService.getUserDetails(userClient, username);
    }
}
