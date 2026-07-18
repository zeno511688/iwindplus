/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeyEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeySaveDTO;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeySearchDTO;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import com.iwindplus.mgt.domain.vo.power.UserExtendYubikeyPageVO;
import com.iwindplus.mgt.domain.vo.power.UserExtendYubikeyVO;
import java.util.List;

/**
 * 用户扩展yubikey业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface UserExtendYubikeyService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserExtendYubikeySaveDTO entity);

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
    boolean edit(UserExtendYubikeyEditDTO entity);

    /**
     * 用户的yubikey列表.
     *
     * @param entity 对象
     * @return IPage<UserExtendYubikeyPageVO>
     */
    IPage<UserExtendYubikeyPageVO> pageByUserId(UserExtendYubikeySearchDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return UserYubikeyVO
     */
    UserExtendYubikeyVO getDetail(Long id);

    /**
     * 根据用户主键查询.
     *
     * @param userId  用户主键
     * @param bizType 业务类型
     * @return UserYubikeyVO
     */
    UserExtendYubikeyVO getByUserId(Long userId, YubikeyBizTypeEnum bizType);
}
