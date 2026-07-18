/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.server.dal.mapper.system.ThirdBindGrantMapper;
import com.iwindplus.mgt.server.dal.model.system.ThirdBindGrantDO;
import org.springframework.stereotype.Repository;

/**
 * 第三方绑定聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ThirdBindGrantRepository extends JoinCrudRepository<ThirdBindGrantMapper, ThirdBindGrantDO> {

}
