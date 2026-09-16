/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.im.application.service.dto.ChatGroupUserSaveDTO;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupUserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聊天群用户业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ChatGroupUserApplicationService {

    private final ChatGroupUserRepository chatGroupUserRepository;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(ChatGroupUserSaveDTO entity) {
        return this.chatGroupUserRepository.save(entity);
    }

    /**
     * 批量添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    public boolean saveBatch(List<ChatGroupUserSaveDTO> entities) {
        return this.chatGroupUserRepository.saveBatch(entities);
    }

    /**
     * 通过主键真实删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeChatGroupByIds(List<Long> ids) {
        return CollUtil.isNotEmpty(ids) && SqlHelper.retBool(this.chatGroupUserRepository.getBaseMapper().deleteByIds(ids));
    }

    /**
     * 通过聊天群主键真实删除.
     *
     * @param chatGroupIds 聊天群主键集合
     * @return boolean
     */
    public boolean removeByChatGroupIds(List<Long> chatGroupIds) {
        return CollUtil.isNotEmpty(chatGroupIds) && SqlHelper.retBool(
            this.chatGroupUserRepository.getBaseMapper().deleteByChatGroupIds(chatGroupIds));
    }
}
