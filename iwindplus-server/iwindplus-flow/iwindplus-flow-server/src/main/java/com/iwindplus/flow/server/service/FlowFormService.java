/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.flow.domain.dto.FlowFormEditDTO;
import com.iwindplus.flow.domain.dto.FlowFormSaveDTO;
import com.iwindplus.flow.domain.dto.FlowFormSearchDTO;
import com.iwindplus.flow.domain.vo.FlowFormBaseExtendVO;
import com.iwindplus.flow.domain.vo.FlowFormPageVO;
import com.iwindplus.flow.domain.vo.FlowFormVO;
import java.util.List;

/**
 * 流程表单业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowFormService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowFormSaveDTO entity);

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
    boolean edit(FlowFormEditDTO entity);

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
     * 分页查询.
     *
     * @param entity 对象
     * @return IPage<FlowFormPageVO>
     */
    IPage<FlowFormPageVO> page(FlowFormSearchDTO entity);

    /**
     * 查询启用的.
     *
     * @return List<FlowFormBaseExtendVO>
     */
    List<FlowFormBaseExtendVO> listEnabled();

    /**
     * 通过主键端查找.
     *
     * @param id 主键
     * @return FlowFormVO
     */
    FlowFormVO getDetail(Long id);
}
