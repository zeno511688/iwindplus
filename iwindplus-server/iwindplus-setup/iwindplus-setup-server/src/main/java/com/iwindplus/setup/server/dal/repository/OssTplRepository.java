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
import com.iwindplus.setup.server.dal.mapper.OssTplMapper;
import com.iwindplus.setup.server.dal.model.OssTplDO;
import org.springframework.stereotype.Repository;

/**
 * 对象存储模板聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class OssTplRepository extends JoinCrudRepository<OssTplMapper, OssTplDO> {

    /**
     * 检查名称是否已存在.
     *
     * @param name  名称
     * @param orgId 组织主键
     */
    public void getNameIsExist(String name, Long orgId) {
        final LambdaQueryWrapper<OssTplDO> queryWrapper = Wrappers.lambdaQuery(OssTplDO.class)
            .eq(OssTplDO::getOrgId, orgId)
            .eq(OssTplDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查空间名是否已存在.
     *
     * @param bucketName 空间名
     * @param orgId      组织主键
     */
    public void getBucketNameIsExist(String bucketName, Long orgId) {
        final LambdaQueryWrapper<OssTplDO> queryWrapper = Wrappers.lambdaQuery(OssTplDO.class)
            .eq(OssTplDO::getOrgId, orgId)
            .eq(OssTplDO::getBucketName, bucketName);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(SetupCodeEnum.BUCKET_NAME_EXIST);
        }
    }

    /**
     * 检查编码是否已存在.
     *
     * @param code  编码
     * @param orgId 组织主键
     */
    public void getCodeIsExist(String code, Long orgId) {
        final LambdaQueryWrapper<OssTplDO> queryWrapper = Wrappers.lambdaQuery(OssTplDO.class)
            .eq(OssTplDO::getOrgId, orgId)
            .eq(OssTplDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

}
