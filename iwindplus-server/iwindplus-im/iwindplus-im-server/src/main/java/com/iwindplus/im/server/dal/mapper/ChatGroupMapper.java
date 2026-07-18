/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.dal.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.im.domain.dto.ChatGroupSearchDTO;
import com.iwindplus.im.domain.vo.ChatGroupPageVO;
import com.iwindplus.im.server.dal.model.ChatGroupDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 聊天群数据访问层接口类.
 *
 * @author zengdegui
 * @since 2023/11/09 20:08
 */
@Mapper
public interface ChatGroupMapper extends MPJBaseMapper<ChatGroupDO> {

    /**
     * 真实删除.
     *
     * @param ids 主键集合
     * @return int
     */
    int deleteByIds(@Param(Constants.LIST) List<Long> ids);

    /**
     * 获取用户聊天群（分页查询）.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ChatGroupPageVO>
     */
    IPage<ChatGroupPageVO> selectPageByCondition(PageDTO<ChatGroupDO> page, @Param(Constants.WRAPPER) ChatGroupSearchDTO entity);

    /**
     * 根据用户主键查询.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<Long>
     */
    List<Long> selectByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);
}
