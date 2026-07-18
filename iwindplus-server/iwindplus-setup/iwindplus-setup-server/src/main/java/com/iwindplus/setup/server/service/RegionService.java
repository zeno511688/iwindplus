/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import cn.hutool.core.lang.tree.Tree;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.setup.domain.dto.RegionEditDTO;
import com.iwindplus.setup.domain.dto.RegionSaveDTO;
import com.iwindplus.setup.domain.vo.RegionVO;
import java.util.List;

/**
 * 省市区业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface RegionService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(RegionSaveDTO entity);

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
    boolean edit(RegionEditDTO entity);

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
     * 通过状态查询.
     *
     * @param status 状态
     * @return List<Tree < Long>>
     */
    List<Tree<Long>> listByEnabled(EnableStatusEnum status);

    /**
     * 详情.
     *
     * @param id 主键
     * @return RegionVO
     */
    RegionVO getDetail(Long id);
}
