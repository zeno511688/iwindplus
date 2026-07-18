/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.auth.domain.dto.OauthUserDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserDetailVO;
import java.util.Set;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户密码认证业务层接口.
 *
 * @author zengdegui 2024/12/4 10:55
 */
public interface SysUserDetailsService extends UserDetailsService {

    /**
     * UserDetails.
     *
     * @param userClient userClient
     * @param param      param
     * @return UserDetails
     */
    static UserDetails getUserDetails(UserClient userClient, String param) {
        ResultVO<UserDetailVO> result = userClient.getLoginByParam(param);
        result.errorThrow();
        final UserDetailVO data = result.getBizData();
        return getUserDetails(data);
    }

    /**
     * UserDetails.
     *
     * @param data data
     * @return UserDetails
     */
    static UserDetails getUserDetails(UserDetailVO data) {
        final OauthUserDTO result = BeanUtil.copyProperties(data, OauthUserDTO.class);
        result.setEnabled(data.getEnabled());
        final Set<String> permissions = data.getPermissions();
        if (CollUtil.isNotEmpty(permissions)) {
            result.addGrantedAuthority(permissions);
        }
        return result;
    }
}
