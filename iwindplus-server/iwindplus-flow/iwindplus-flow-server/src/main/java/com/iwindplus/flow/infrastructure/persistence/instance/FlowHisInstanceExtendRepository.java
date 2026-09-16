/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.infrastructure.persistence.instance;

import com.github.yulichang.repository.JoinCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 历史流程实例扩展聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class FlowHisInstanceExtendRepository extends JoinCrudRepository<FlowHisInstanceExtendMapper, FlowHisInstanceExtendDO> {

}
