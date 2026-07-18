/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.DepartmentEditDTO;
import com.iwindplus.mgt.domain.dto.power.DepartmentSaveDTO;
import com.iwindplus.mgt.domain.vo.power.DepartmentBaseVO;
import com.iwindplus.mgt.domain.vo.power.DepartmentExtendVO;
import java.util.List;

/**
 * 部门业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface DepartmentService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(DepartmentSaveDTO entity);

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
    boolean edit(DepartmentEditDTO entity);

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
     * 获取组织部门.
     *
     * @param orgId 组织主键
     * @return List<Tree < Long>>
     */
    List<Tree<Long>> listByOrgId(Long orgId);

    /**
     * 获取启用的组织部门.
     *
     * @param orgId 组织主键
     * @return List<Tree < Long>>
     */
    List<Tree<Long>> listEnabledByOrgId(Long orgId);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return DepartmentExtendVO
     */
    DepartmentExtendVO getDetailExtend(Long id);

    /**
     * 查询用户所属部门.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<RoleBaseVO>
     */
    List<DepartmentBaseVO> listCheckedByUserId(Long orgId, Long userId);
}
