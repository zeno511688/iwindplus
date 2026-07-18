/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.system.AppCertMapper;
import com.iwindplus.mgt.server.dal.model.system.AppCertDO;
import org.springframework.stereotype.Repository;

/**
 * 应用凭证聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class AppCertRepository extends JoinCrudRepository<AppCertMapper, AppCertDO> {

    /**
     * 检查名称是否存在.
     *
     * @param name 名称
     */
    public void getNameIsExist(String name) {
        final LambdaQueryWrapper<AppCertDO> queryWrapper = Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查访问key是否存在.
     *
     * @param accessKey 访问key
     */
    public void getAccessKeyIsExist(String accessKey) {
        final LambdaQueryWrapper<AppCertDO> queryWrapper = Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getName, accessKey);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(BizCodeEnum.ACCESS_KEY_EXIST);
        }
    }

    /**
     * 检查应用凭证类型是否存在.
     *
     * @param appCertType 应用凭证类型
     */
    public void getAppCertTypeIsExist(AppCertTypeEnum appCertType) {
        final LambdaQueryWrapper<AppCertDO> queryWrapper = Wrappers.lambdaQuery(AppCertDO.class)
            .eq(AppCertDO::getCertType, appCertType);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.APP_CERT_TYPE_EXIST);
        }
    }

}
