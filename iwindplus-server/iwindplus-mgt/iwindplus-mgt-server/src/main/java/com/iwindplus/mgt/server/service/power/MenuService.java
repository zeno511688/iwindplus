/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.MenuEditDTO;
import com.iwindplus.mgt.domain.dto.power.MenuSaveDTO;
import com.iwindplus.mgt.domain.vo.power.MenuExtendVO;
import com.iwindplus.mgt.domain.vo.power.MenuTreeSystemVO;
import com.iwindplus.mgt.domain.vo.power.MenuVO;
import java.util.List;

/**
 * 菜单业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface MenuService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(MenuSaveDTO entity);

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
    boolean edit(MenuEditDTO entity);

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
     * 通过系统主键查询.
     *
     * @param systemId 系统主键
     * @return List<Tree < Long>>
     */
    List<Tree<Long>> listBySystemId(Long systemId);

    /**
     * 通过系统主键查询启用的.
     *
     * @param systemId 系统主键
     * @return List<Tree < Long>>
     */
    List<Tree<Long>> listEnabledBySystemId(Long systemId);

    /**
     * 用户菜单权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<MenuTreeSystemVO>
     */
    List<MenuTreeSystemVO> listByUserId(Long orgId, Long userId);

    /**
     * 通过角色查询.
     *
     * @param orgId  组织主键
     * @param roleId 角色主键
     * @return List<MenuTreeSystemVO>
     */
    List<MenuTreeSystemVO> listByRoleId(Long orgId, Long roleId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return MenuVO
     */
    MenuVO getDetail(Long id);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return MenuExtendVO
     */
    MenuExtendVO getDetailExtend(Long id);

}
