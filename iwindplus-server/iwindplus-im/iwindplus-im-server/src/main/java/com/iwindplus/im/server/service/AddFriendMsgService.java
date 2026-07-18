/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.im.domain.dto.AddFriendMsgDTO;
import com.iwindplus.im.domain.dto.AddFriendMsgSearchDTO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.vo.AddFriendMsgPageVO;
import com.iwindplus.im.domain.vo.AddFriendMsgVO;
import com.iwindplus.im.server.dal.model.AddFriendMsgDO;
import java.util.List;

/**
 * 加好友消息业务层接口类.
 *
 * @author zengdegui
 * @since 202Join25
 */
public interface AddFriendMsgService extends EsBaseService<AddFriendMsgDO> {

    /**
     * 添加.
     *
     * @param entity   对象
     * @return boolean
     */
    boolean save(AddFriendMsgDTO entity);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(AddFriendMsgDTO entity);

    /**
     * 编辑消息状态.
     *
     * @param id        主键
     * @param msgStatus 消息状态
     * @return boolean
     */
    boolean editMsgStatus(String id, MsgStatusEnum msgStatus);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<String> ids);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<AddFriendMsgPageVO>
     */
    IPage<AddFriendMsgPageVO> page(PageDTO<AddFriendMsgDO> page, AddFriendMsgSearchDTO entity);

    /**
     * 查询未接收到的.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return List<AddFriendMsgVO>
     */
    List<AddFriendMsgVO> listByUnSendSuccess(Long userId, Long orgId);

    /**
     * 详情.
     *
     * @param id      主键
     * @param ossTplCode 对象存储模板配置编码
     * @return AddFriendMsgDO
     */
    AddFriendMsgVO getDetail(String id, String ossTplCode);
}
