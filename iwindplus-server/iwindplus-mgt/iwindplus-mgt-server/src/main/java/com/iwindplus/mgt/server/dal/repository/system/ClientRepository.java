/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.server.dal.mapper.system.ClientMapper;
import com.iwindplus.mgt.server.dal.model.system.ClientDO;
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

}
