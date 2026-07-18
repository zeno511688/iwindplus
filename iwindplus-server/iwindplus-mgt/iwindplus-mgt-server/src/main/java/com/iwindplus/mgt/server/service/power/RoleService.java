/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.RoleDTO;
import com.iwindplus.mgt.domain.dto.power.RoleEditDTO;
import com.iwindplus.mgt.domain.dto.power.RoleSaveDTO;
import com.iwindplus.mgt.domain.dto.power.RoleSearchDTO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.domain.vo.power.RoleExtendVO;
import com.iwindplus.mgt.domain.vo.power.RolePageVO;
import com.iwindplus.mgt.domain.vo.power.RoleVO;
import java.util.List;
import java.util.Set;

/**
 * 角色业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface RoleService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(RoleSaveDTO entity);

    /**
     * 批量初始化.
     *
     * @param entities 对象集合
     * @return List<RoleVO>
     */
    List<RoleVO> saveBatchInit(List<RoleDTO> entities);

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
    boolean edit(RoleEditDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, EnableStatusEnum status);

    /**
     * 编辑设为默认.
     *
     * @param id    主键
     * @param orgId 组织主键
     * @return boolean
     */
    boolean editDefault(Long id, Long orgId);

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
     * @return IPage<RolePageVO>
     */
    IPage<RolePageVO> page(RoleSearchDTO entity);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return RoleExtendVO
     */
    RoleExtendVO getDetailExtend(Long id);

    /**
     * 通过用户主键获取组织角色（标记选中）.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<RoleBaseCheckedVO>
     */
    List<RoleBaseCheckedVO> listByUserId(Long orgId, Long userId);

    /**
     * 通过用户组主键获取组织角色（标记选中）.
     *
     * @param orgId       组织主键
     * @param userGroupId 用户组主键
     * @return List<RoleBaseCheckedVO>
     */
    List<RoleBaseCheckedVO> listByUserGroupId(Long orgId, Long userGroupId);

    /**
     * 查询用户角色权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<RoleBaseVO>
     */
    List<RoleBaseVO> listCheckedByUserId(Long orgId, Long userId);

    /**
     * 查询默认角色.
     *
     * @param orgId 组织主键
     * @return List<Long>
     */
    Set<Long> listDefaultRoles(Long orgId);
}
