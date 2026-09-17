/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.system.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.application.query.system.app.dto.ClientSearchDTO;
import com.iwindplus.mgt.application.query.system.app.vo.ClientPageVO;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * 客户端聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ClientRepository extends JoinCrudRepository<ClientMapper, ClientDO> {

    /**
     * 默认的认证方式
     */
    public static final Set<String> DEFAULT_AUTHENTICATION_METHOD = Set.of("client_secret_basic", "client_secret_post");

    /**
     * 默认的权限范围
     */
    public static final Set<String> DEFAULT_SCOPE = Set.of("profile", "mobile", "email");

    /**
     * 默认的token格式
     */
    public static final String DEFAULT_ACCESS_TOKEN_FORMAT = "self-contained";

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ClientPageVO> page(ClientSearchDTO entity) {
        PageDTO<ClientDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ClientDO> queryWrapper = Wrappers.lambdaQuery(ClientDO.class)
            .orderByDesc(ClientDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ClientDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getClientId())) {
            queryWrapper.eq(ClientDO::getClientId, entity.getClientId().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getClientName())) {
            queryWrapper.eq(ClientDO::getClientName, entity.getClientName().trim());
        }
        queryWrapper.select(ClientDO::getId, ClientDO::getCreatedBy, ClientDO::getCreatedTimestamp, ClientDO::getModifiedTimestamp,
            ClientDO::getModifiedBy, ClientDO::getVersion, ClientDO::getStatus, ClientDO::getClientId, ClientDO::getClientName,
            ClientDO::getClientIdIssuedAt, ClientDO::getClientSecretExpiresAt);
        final PageDTO<ClientDO> modelPage = super.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ClientPageVO.class));
    }

}
