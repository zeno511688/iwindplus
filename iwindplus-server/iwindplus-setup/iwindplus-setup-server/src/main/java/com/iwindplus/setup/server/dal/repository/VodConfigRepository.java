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
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.server.dal.mapper.VodConfigMapper;
import com.iwindplus.setup.server.dal.model.VodConfigDO;
import org.springframework.stereotype.Repository;

/**
 * 视频点播配置聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class VodConfigRepository extends JoinCrudRepository<VodConfigMapper, VodConfigDO> {

    /**
     * 检查名称是否已存在.
     *
     * @param name  名称
     * @param type  类型
     * @param orgId 组织主键
     */
    public void getNameIsExist(String name, VodTypeEnum type, Long orgId) {
        final LambdaQueryWrapper<VodConfigDO> queryWrapper = Wrappers.lambdaQuery(VodConfigDO.class)
            .eq(VodConfigDO::getOrgId, orgId)
            .eq(VodConfigDO::getType, type)
            .eq(VodConfigDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查区域是否已存在.
     *
     * @param region 区域
     * @param type   类型
     * @param orgId  组织主键
     */
    public void getRegionIsExist(String region, VodTypeEnum type, Long orgId) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(VodConfigDO.class)
            .eq(VodConfigDO::getType, type)
            .eq(VodConfigDO::getRegion, region.trim())
            .eq(VodConfigDO::getOrgId, orgId)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(SetupCodeEnum.REGION_EXIST);
        }
    }

    /**
     * 检查编码是否已存在.
     *
     * @param code  编码
     * @param type  类型
     * @param orgId 组织主键
     */
    public void getCodeIsExist(String code, VodTypeEnum type, Long orgId) {
        final LambdaQueryWrapper<VodConfigDO> queryWrapper = Wrappers.lambdaQuery(VodConfigDO.class)
            .eq(VodConfigDO::getOrgId, orgId)
            .eq(VodConfigDO::getType, type)
            .eq(VodConfigDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

}
