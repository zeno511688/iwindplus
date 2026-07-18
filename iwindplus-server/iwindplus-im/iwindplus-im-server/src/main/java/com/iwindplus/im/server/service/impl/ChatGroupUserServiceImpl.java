/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.im.domain.dto.ChatGroupUserSaveDTO;
import com.iwindplus.im.domain.dto.ChatGroupUserSearchDTO;
import com.iwindplus.im.domain.vo.ChatGroupUserPageVO;
import com.iwindplus.im.server.dal.model.ChatGroupUserDO;
import com.iwindplus.im.server.dal.repository.ChatGroupUserRepository;
import com.iwindplus.im.server.service.ChatGroupUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聊天群用户业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ChatGroupUserServiceImpl implements ChatGroupUserService {

    private final ChatGroupUserRepository chatGroupUserRepository;

    @Override
    public boolean save(ChatGroupUserSaveDTO entity) {
        return this.chatGroupUserRepository.save(entity);
    }

    @Override
    public boolean saveBatch(List<ChatGroupUserSaveDTO> entities) {
        return this.chatGroupUserRepository.saveBatch(entities);
    }

    @Override
    public boolean removeChatGroupByIds(List<Long> ids) {
        return CollUtil.isNotEmpty(ids) && SqlHelper.retBool(this.chatGroupUserRepository.getBaseMapper().deleteByIds(ids));
    }

    @Override
    public boolean removeByChatGroupIds(List<Long> chatGroupIds) {
        return CollUtil.isNotEmpty(chatGroupIds) && SqlHelper.retBool(this.chatGroupUserRepository.getBaseMapper().deleteByChatGroupIds(chatGroupIds));
    }

    @Override
    public IPage<ChatGroupUserPageVO> page(PageDTO<ChatGroupUserDO> page, ChatGroupUserSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.chatGroupUserRepository.getBaseMapper().selectPageByCondition(page, entity);
    }

}
