/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.service;

import com.iwindplus.base.shiro.domain.vo.AccessPermsVO;
import com.iwindplus.base.shiro.domain.vo.ShiroUserVO;

import java.util.List;

/**
 * shiro业务层接口.
 *
 * @author zengdegui
 * @since 2019/4/17
 */
public interface ShiroService {
    /**
     * 通过用户名查找.
     *
     * @param username 用户名
     * @return ShiroUserVO
     */
    ShiroUserVO getByUsername(String username);

    /**
     * 查找访问权限.
     *
     * @return List<AccessPermsVO>
     */
    List<AccessPermsVO> listAccessPerms();
}
