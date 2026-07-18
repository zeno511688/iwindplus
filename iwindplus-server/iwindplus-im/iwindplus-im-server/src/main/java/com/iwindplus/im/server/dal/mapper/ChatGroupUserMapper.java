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
import com.iwindplus.im.domain.dto.ChatGroupUserSearchDTO;
import com.iwindplus.im.domain.vo.ChatGroupUserPageVO;
import com.iwindplus.im.server.dal.model.ChatGroupUserDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 聊天群用户数据访问层接口类.
 *
 * @author zengdegui
 * @since 2023/11/09 20:08
 */
@Mapper
public interface ChatGroupUserMapper extends MPJBaseMapper<ChatGroupUserDO> {
    /**
     * 真实删除.
     *
     * @param chatGroupIds 聊天群主键集合
     * @return int
     */
    int deleteByChatGroupIds(@Param(Constants.LIST) List<Long> chatGroupIds);

    /**
     * 真实删除.
     *
     * @param entities 主键集合
     * @return int
     */
    int deleteByIds(@Param(Constants.LIST) List<Long> entities);

    /**
     * 获取聊天群用户（分页查询）.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ChatGroupUserPageVO>
     */
    IPage<ChatGroupUserPageVO> selectPageByCondition(PageDTO<ChatGroupUserDO> page, @Param(Constants.WRAPPER) ChatGroupUserSearchDTO entity);
}
