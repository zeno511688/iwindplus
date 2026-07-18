/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.dal.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.mapper.WechatConfigMaMapper;
import com.iwindplus.setup.server.dal.model.WechatConfigMaDO;
import org.springframework.stereotype.Repository;

/**
 * 微信小程序配置聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class WechatConfigMaRepository extends JoinCrudRepository<WechatConfigMaMapper, WechatConfigMaDO> {

    /**
     * 检查名称是否已存在.
     *
     * @param name  名称
     * @param orgId 组织主键
     */
    public void getNameIsExist(String name, Long orgId) {
        final LambdaQueryWrapper<WechatConfigMaDO> queryWrapper = Wrappers.lambdaQuery(WechatConfigMaDO.class)
            .eq(WechatConfigMaDO::getOrgId, orgId)
            .eq(WechatConfigMaDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查访问key是否已存在.
     *
     * @param accessKey 访问key
     * @param orgId     组织主键
     */
    public void getAccessKeyIsExist(String accessKey, Long orgId) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(WechatConfigMaDO.class)
            .eq(WechatConfigMaDO::getAccessKey, accessKey)
            .eq(WechatConfigMaDO::getOrgId, orgId)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(SetupCodeEnum.WECHAT_CONFIG_MA_ACCESS_KEY_EXIST);
        }
    }

    /**
     * 检查编码是否已存在.
     *
     * @param code  编码
     * @param orgId 组织主键
     */
    public void getCodeIsExist(String code, Long orgId) {
        final LambdaQueryWrapper<WechatConfigMaDO> queryWrapper = Wrappers.lambdaQuery(WechatConfigMaDO.class)
            .eq(WechatConfigMaDO::getOrgId, orgId)
            .eq(WechatConfigMaDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

}
