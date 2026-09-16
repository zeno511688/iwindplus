/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.query;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.im.application.query.dto.ChatGroupUserSearchDTO;
import com.iwindplus.im.application.query.vo.ChatGroupUserPageVO;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupUserDO;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天群用户查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGroupUserQueryService {

    private final ChatGroupUserRepository chatGroupUserRepository;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ChatGroupUserPageVO>
     */
    public IPage<ChatGroupUserPageVO> page(PageDTO<ChatGroupUserDO> page, ChatGroupUserSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.chatGroupUserRepository.getBaseMapper().selectPageByCondition(page, entity);
    }
}
