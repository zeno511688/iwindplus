/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.flow.domain.dto.FlowCategoryEditDTO;
import com.iwindplus.flow.domain.dto.FlowCategorySaveDTO;
import com.iwindplus.flow.domain.dto.FlowCategorySearchDTO;
import com.iwindplus.flow.domain.vo.FlowCategoryBaseVO;
import com.iwindplus.flow.domain.vo.FlowCategoryPageVO;
import com.iwindplus.flow.domain.vo.FlowCategoryVO;
import java.util.List;

/**
 * 流程分类业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowCategoryService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowCategorySaveDTO entity);

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
    boolean edit(FlowCategoryEditDTO entity);

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
     * @return IPage<FlowCategoryPageVO>
     */
    IPage<FlowCategoryPageVO> page(FlowCategorySearchDTO entity);

    /**
     * 查询启用的.
     *
     * @return List<FlowCategoryBaseVO>
     */
    List<FlowCategoryBaseVO> listEnabled();

    /**
     * 通过主键端查找.
     *
     * @param id 主键
     * @return FlowCategoryVO
     */
    FlowCategoryVO getDetail(Long id);
}
