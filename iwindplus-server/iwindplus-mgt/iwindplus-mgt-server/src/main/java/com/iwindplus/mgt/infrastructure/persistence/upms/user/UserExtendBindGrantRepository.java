/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.upms.user;

import com.github.yulichang.repository.JoinCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户扩展绑定聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserExtendBindGrantRepository extends JoinCrudRepository<UserExtendBindGrantMapper, UserExtendBindGrantDO> {

}
