/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.constant.CommonConstant.UserConstant;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.mybatis.domain.property.MybatisProperty;
import jakarta.annotation.Resource;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

/**
 * 公共字段自动填充.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class MyBatisAutoFillHandler implements MetaObjectHandler {

    @Resource
    private MybatisProperty property;

    /**
     * 添加填充.
     *
     * @param metaObject metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        if (Boolean.FALSE.equals(this.property.getField().getFill().getEnabled())) {
            return;
        }

        final int numberZero = NumberConstant.NUMBER_ZERO;
        final UserBaseVO userInfo = this.getCurrentUserInfo();
        final Long userId = userInfo.getUserId();
        final Long orgId = userInfo.getOrgId();
        final String realName = userInfo.getRealName();
        if (Boolean.TRUE.equals(this.property.getField().getFill().getEnabledInsertStrict())) {
            this.buildStrictInsertFill(metaObject, userId, realName);
        } else {
            this.buildInsertFill(metaObject, userId, realName);
        }
        // 设置是否删除（仅未设置时填充）.
        this.strictInsertFill(metaObject, DbConstant.DELETED, () -> numberZero, Integer.class);
        // 设置乐观锁（仅未设置时填充）.
        this.strictInsertFill(metaObject, DbConstant.VERSION, () -> numberZero, Integer.class);
        // 用户主键.
        this.strictInsertFill(metaObject, UserConstant.USER_ID, () -> userId, Long.class);
        // 组织主键.
        this.strictInsertFill(metaObject, UserConstant.ORG_ID, () -> orgId, Long.class);
    }

    /**
     * 更新填充.
     *
     * @param metaObject metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        if (Boolean.FALSE.equals(this.property.getField().getFill().getEnabled())) {
            return;
        }

        final long currentTimeMillis = System.currentTimeMillis();
        final UserBaseVO userInfo = this.getCurrentUserInfo();
        final Long userId = userInfo.getUserId();
        final String realName = userInfo.getRealName();
        // 设置更新时间戳（仅未设置时填充，业务显式赋值优先）.
        this.strictUpdateFill(metaObject, DbConstant.MODIFIED_TIMESTAMP, () -> currentTimeMillis, Long.class);
        // 设置更新人和更新人主键（userId > 0时填充）.
        if (userId != null && userId > 0) {
            this.strictUpdateFill(metaObject, DbConstant.MODIFIED_BY, () -> realName, String.class);
            this.strictUpdateFill(metaObject, DbConstant.MODIFIED_ID, () -> userId, Long.class);
        }
    }

    private UserBaseVO getCurrentUserInfo() {
        return Optional.ofNullable(UserContextHolder.getContext()).orElse(UserContextHolder.getDefaultUser());
    }

    private void buildStrictInsertFill(MetaObject metaObject, Long userId, String realName) {
        final long currentTimeMillis = System.currentTimeMillis();
        // 强制设置创建时间戳.
        this.setFieldValByName(DbConstant.CREATED_TIMESTAMP, currentTimeMillis, metaObject);
        // 强制设置更新时间戳.
        this.setFieldValByName(DbConstant.MODIFIED_TIMESTAMP, currentTimeMillis, metaObject);
        // 设置创建人.
        this.setFieldValByName(DbConstant.CREATED_BY, realName, metaObject);
        // 设置更新人.
        this.setFieldValByName(DbConstant.MODIFIED_BY, realName, metaObject);
        if (userId != null && userId > 0) {
            this.setFieldValByName(DbConstant.CREATED_ID, userId, metaObject);
            this.setFieldValByName(DbConstant.MODIFIED_ID, userId, metaObject);
        }
    }

    private void buildInsertFill(MetaObject metaObject, Long userId, String realName) {
        final long currentTimeMillis = System.currentTimeMillis();
        // 设置创建时间戳（仅未设置时填充，业务显式赋值优先）.
        this.strictInsertFill(metaObject, DbConstant.CREATED_TIMESTAMP, () -> currentTimeMillis, Long.class);
        // 设置更新时间戳（仅未设置时填充，业务显式赋值优先）.
        this.strictInsertFill(metaObject, DbConstant.MODIFIED_TIMESTAMP, () -> currentTimeMillis, Long.class);
        // 设置创建人.
        this.strictInsertFill(metaObject, DbConstant.CREATED_BY, () -> realName, String.class);
        // 设置更新人.
        this.strictInsertFill(metaObject, DbConstant.MODIFIED_BY, () -> realName, String.class);
        // 设置创建人/更新人主键（userId > 0时填充）.
        if (userId != null && userId > 0) {
            this.strictInsertFill(metaObject, DbConstant.CREATED_ID, () -> userId, Long.class);
            this.strictInsertFill(metaObject, DbConstant.MODIFIED_ID, () -> userId, Long.class);
        }
    }
}
