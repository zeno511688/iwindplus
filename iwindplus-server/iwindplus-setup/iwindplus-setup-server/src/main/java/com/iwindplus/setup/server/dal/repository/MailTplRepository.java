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
import com.iwindplus.setup.server.dal.mapper.MailTplMapper;
import com.iwindplus.setup.server.dal.model.MailTplDO;
import org.springframework.stereotype.Repository;

/**
 * 邮箱模板聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class MailTplRepository extends JoinCrudRepository<MailTplMapper, MailTplDO> {

    /**
     * 检查名称是否已存在.
     *
     * @param name  名称
     * @param orgId 组织主键
     */
    public void getNameIsExist(String name, Long orgId) {
        final LambdaQueryWrapper<MailTplDO> queryWrapper = Wrappers.lambdaQuery(MailTplDO.class)
            .eq(MailTplDO::getOrgId, orgId)
            .eq(MailTplDO::getName, name);
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
        final LambdaQueryWrapper<MailTplDO> queryWrapper = Wrappers.lambdaQuery(MailTplDO.class)
            .eq(MailTplDO::getOrgId, orgId)
            .eq(MailTplDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

}
