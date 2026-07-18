/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.dal.repository;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.im.server.dal.mapper.UserFriendMapper;
import com.iwindplus.im.server.dal.model.UserFriendDO;
import org.springframework.stereotype.Repository;

/**
 * 用户好友聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserFriendRepository extends JoinCrudRepository<UserFriendMapper, UserFriendDO> {
    
}
