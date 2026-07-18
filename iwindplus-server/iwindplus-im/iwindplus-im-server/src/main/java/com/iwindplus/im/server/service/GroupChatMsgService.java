/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.im.domain.dto.GroupChatMsgDTO;
import com.iwindplus.im.domain.dto.GroupChatMsgSearchDTO;
import com.iwindplus.im.domain.vo.GroupChatMsgPageVO;
import com.iwindplus.im.domain.vo.GroupChatMsgVO;
import com.iwindplus.im.server.dal.model.GroupChatMsgDO;
import java.util.List;

/**
 * 群聊消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface GroupChatMsgService extends EsBaseService<GroupChatMsgDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(GroupChatMsgDTO entity);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(GroupChatMsgDTO entity);

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
     * @return IPage<GroupChatMsgPageVO>
     */
    IPage<GroupChatMsgPageVO> page(PageDTO<GroupChatMsgDO> page, GroupChatMsgSearchDTO entity);

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return GroupChatMsgVO
     */
    GroupChatMsgVO getDetail(String id, String ossTplCode);
}
