/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.upms.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.application.query.upms.user.dto.UserExtendBindGrantSearchDTO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantPageVO;
import org.springframework.stereotype.Repository;

/**
 * 用户扩展绑定聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserExtendBindGrantRepository extends JoinCrudRepository<UserExtendBindGrantMapper, UserExtendBindGrantDO> {

    /**
     * 分页查询用户扩展绑定授权.
     *
     * @param entity 查询条件
     * @return 分页查询结果
     */
    public IPage<UserExtendBindGrantPageVO> page(UserExtendBindGrantSearchDTO entity) {
        PageDTO<UserExtendBindGrantDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return super.getBaseMapper().selectPageByCondition(page, entity);
    }
}
