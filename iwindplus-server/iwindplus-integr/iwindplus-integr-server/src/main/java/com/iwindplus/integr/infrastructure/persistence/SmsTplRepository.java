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
import com.iwindplus.integr.application.query.vo.SmsTplVO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * 短信模板聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class SmsTplRepository extends JoinCrudRepository<SmsTplMapper, SmsTplDO> {

    /**
     * 检查名称是否已存在.
     *
     * @param name  名称
     * @param orgId 组织主键
     */
    public void getNameIsExist(String name, Long orgId) {
        final LambdaQueryWrapper<SmsTplDO> queryWrapper = Wrappers.lambdaQuery(SmsTplDO.class)
            .eq(SmsTplDO::getOrgId, orgId)
            .eq(SmsTplDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查编码是否已存在.
     *
     * @param code  编码
     * @param orgId 组织主键
     */
    public void getCodeIsExist(String code, Long orgId) {
        final LambdaQueryWrapper<SmsTplDO> queryWrapper = Wrappers.lambdaQuery(SmsTplDO.class)
            .eq(SmsTplDO::getOrgId, orgId)
            .eq(SmsTplDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 根据编码获取对象.
     *
     * @param code 编码
     * @return SmsTplVO
     */
    public SmsTplVO getByCode(String code) {
        SmsTplDO data = super.getOne(Wrappers.lambdaQuery(SmsTplDO.class)
            .eq(SmsTplDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(IntegrCodeEnum.SMS_TEMPLATE_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(IntegrCodeEnum.SMS_TEMPLATE_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(IntegrCodeEnum.SMS_TEMPLATE_LOCKED);
        }
        final SmsTplVO result = BeanUtil.copyProperties(data, SmsTplVO.class);
        return result;
    }
}
