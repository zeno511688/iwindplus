/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.im.domain.dto.ChatGroupUserSaveDTO;
import com.iwindplus.im.domain.dto.ChatGroupUserSearchDTO;
import com.iwindplus.im.domain.vo.ChatGroupUserPageVO;
import com.iwindplus.im.server.dal.model.ChatGroupUserDO;
import java.util.List;

/**
 * 聊天群用户业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface ChatGroupUserService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(ChatGroupUserSaveDTO entity);

    /**
     * 批量添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<ChatGroupUserSaveDTO> entities);

    /**
     * 通过主键真实删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeChatGroupByIds(List<Long> ids);

    /**
     * 通过聊天群主键真实删除.
     *
     * @param chatGroupIds 聊天群主键集合
     * @return boolean
     */
    boolean removeByChatGroupIds(List<Long> chatGroupIds);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ChatGroupUserPageVO>
     */
    IPage<ChatGroupUserPageVO> page(PageDTO<ChatGroupUserDO> page, ChatGroupUserSearchDTO entity);
}
