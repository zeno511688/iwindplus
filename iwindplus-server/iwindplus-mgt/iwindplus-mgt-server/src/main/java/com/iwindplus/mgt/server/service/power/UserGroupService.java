/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.UserGroupSaveEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserGroupSearchDTO;
import com.iwindplus.mgt.domain.vo.power.UserGroupBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupExtendVO;
import com.iwindplus.mgt.domain.vo.power.UserGroupPageVO;
import java.util.List;

/**
 * 用户组业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface UserGroupService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserGroupSaveEditDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑（id必选）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(UserGroupSaveEditDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, EnableStatusEnum status);

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    boolean editBuildIn(Long id, Boolean buildInFlag);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<UserGroupPageVO>
     */
    IPage<UserGroupPageVO> page(UserGroupSearchDTO entity);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return UserGroupExtendVO
     */
    UserGroupExtendVO getDetailExtend(Long id);

    /**
     * 通过用户主键查询选中的用户组.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<UserGroupBaseCheckedVO>
     */
    List<UserGroupBaseCheckedVO> listByUserId(Long orgId, Long userId);

    /**
     * 通过角色主键查询选中的用户组.
     *
     * @param orgId  组织主键
     * @param roleId 角色主键
     * @return List<UserGroupBaseCheckedVO>
     */
    List<UserGroupBaseCheckedVO> listByRoleId(Long orgId, Long roleId);
}
