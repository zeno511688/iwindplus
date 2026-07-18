/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.im.domain.dto.ChatGroupEditDTO;
import com.iwindplus.im.domain.dto.ChatGroupJoinDTO;
import com.iwindplus.im.domain.dto.ChatGroupSaveDTO;
import com.iwindplus.im.domain.dto.ChatGroupSearchDTO;
import com.iwindplus.im.domain.vo.ChatGroupBaseVO;
import com.iwindplus.im.domain.vo.ChatGroupPageVO;
import com.iwindplus.im.domain.vo.ChatGroupVO;
import com.iwindplus.im.server.dal.model.ChatGroupDO;
import java.util.List;

/**
 * 聊天群业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface ChatGroupService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    ChatGroupVO saveChatGroup(ChatGroupSaveDTO entity);

    /**
     * 邀请加入群.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean saveJoinChatGroup(ChatGroupJoinDTO entity);

    /**
     * 删除（解散或退出聊天群）.
     *
     * @param id            主键
     * @param currentUserId 当前登录用户主键
     * @return boolean
     */
    boolean removeChatGroup(Long id, Long currentUserId);

    /**
     * 编辑（id必选）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(ChatGroupEditDTO entity);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ChatGroupPageVO>
     */
    IPage<ChatGroupPageVO> page(PageDTO<ChatGroupDO> page, ChatGroupSearchDTO entity);

    /**
     * 通过主键端查找.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return ChatGroupVO
     */
    ChatGroupVO getDetail(Long id, String ossTplCode);

    /**
     * 根据用户主键查询.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return List<Long>
     */
    List<Long> listByUserId(Long userId, Long orgId);

    /**
     * 通过组织主键查询.
     *
     * @param orgId 组织主键
     * @return List<ChatGroupBaseVO>
     */
    List<ChatGroupBaseVO> listByOrgId(Long orgId);
}
