/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.infrastructure.persistence;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.integr.common.enums.IntegrCodeEnum;
import com.iwindplus.integr.application.query.vo.OssTplVO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.Objects;
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
            throw new BizException(IntegrCodeEnum.BUCKET_NAME_EXIST);
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

    /**
     * 根据编码查询对象存储模板信息.
     *
     * @param code 编码
     * @return 对象存储模板信息
     */
    public OssTplVO getByCode(String code) {
        OssTplDO data = super.getOne(Wrappers.lambdaQuery(OssTplDO.class)
            .eq(OssTplDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(IntegrCodeEnum.OSS_TEMPLATE_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(IntegrCodeEnum.OSS_TEMPLATE_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(IntegrCodeEnum.OSS_TEMPLATE_LOCKED);
        }
        final OssTplVO result = BeanUtil.copyProperties(data, OssTplVO.class);
        return result;
    }
}
