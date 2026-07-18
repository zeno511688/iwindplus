/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.im.domain.dto.UserFriendDTO;
import com.iwindplus.im.domain.dto.UserFriendSearchDTO;
import com.iwindplus.im.domain.enums.FriendStatusEnum;
import com.iwindplus.im.domain.vo.UserFriendPageVO;
import com.iwindplus.im.server.dal.model.UserFriendDO;
import java.util.List;

/**
 * 用户好友业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface UserFriendService {

    /**
     * 添加.
     *
     * @param entity   对象
     * @param userInfo 用户信息
     * @return boolean
     */
    boolean save(UserFriendDTO entity, UserBaseVO userInfo);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(UserFriendDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, FriendStatusEnum status);

    /**
     * 我的好友列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<UserFriendPageVO>
     */
    IPage<UserFriendPageVO> page(PageDTO<UserFriendDO> page, UserFriendSearchDTO entity);
}
