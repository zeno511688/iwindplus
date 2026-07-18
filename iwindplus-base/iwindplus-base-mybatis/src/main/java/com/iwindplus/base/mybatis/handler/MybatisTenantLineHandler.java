/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.mybatis.domain.property.MybatisProperty;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

/**
 * 多租户租户处理器.
 *
 * @author zengdegui
 * @since 2025/04/22
 */
@Slf4j
public record MybatisTenantLineHandler(MybatisProperty mybatisProperty) implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        Long tenantId = this.getTenantIdVal();
        if (ObjectUtil.isEmpty(tenantId)) {
            log.warn("无法获取有效的租户id -> Null");
            return new NullValue();
        }
        // 返回固定租户
        return new LongValue(tenantId.toString());
    }

    @Override
    public String getTenantIdColumn() {
        return this.mybatisProperty.getTenant().getTenantIdColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        Long tenantId = this.getTenantIdVal();
        // 判断是否有租户
        if (ObjectUtil.isNotEmpty(tenantId)) {
            // 不需要过滤租户的表
            List<String> ignoredTableList = this.mybatisProperty.getTenant().getIgnoredTable();
            if (ObjectUtil.isNotEmpty(ignoredTableList)) {
                return ignoredTableList.contains(tableName);
            }
            return false;
        }
        return true;
    }

    private Long getTenantIdVal() {
        return Optional.ofNullable(UserContextHolder.getContext()).map(UserBaseVO::getOrgId)
            .orElse(UserContextHolder.getDefaultUser().getOrgId());
    }
}
