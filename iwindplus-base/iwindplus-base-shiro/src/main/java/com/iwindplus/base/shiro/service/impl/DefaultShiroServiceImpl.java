/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.service.impl;

import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.shiro.domain.vo.AccessPermsVO;
import com.iwindplus.base.shiro.domain.vo.ShiroUserVO;
import com.iwindplus.base.shiro.service.ShiroService;
import com.iwindplus.base.util.BeanCopierUtil;
import java.util.List;

/**
 * 默认shiro业务层接口实现类.
 *
 * @author zengdegui
 * @since 2025/06/06 22:12
 */
public class DefaultShiroServiceImpl implements ShiroService {

    @Override
    public ShiroUserVO getByUsername(String username) {
        final UserBaseVO defaultUser = UserContextHolder.getDefaultUser();
        return BeanCopierUtil.copyProperties(defaultUser, ShiroUserVO::new);
    }

    @Override
    public List<AccessPermsVO> listAccessPerms() {
        return null;
    }
}
