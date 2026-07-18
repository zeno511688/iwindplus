/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.flow.domain.dto.FlowModelEditDTO;
import com.iwindplus.flow.domain.dto.FlowModelSaveDTO;
import com.iwindplus.flow.domain.dto.FlowModelSearchDTO;
import com.iwindplus.flow.domain.enums.FlowModelStatusEnum;
import com.iwindplus.flow.domain.vo.FlowModelExtVO;
import com.iwindplus.flow.domain.vo.FlowModelPageVO;
import java.util.List;

/**
 * 流程模型业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowModelService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(FlowModelSaveDTO entity);

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
    boolean edit(FlowModelEditDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, FlowModelStatusEnum status);

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
     * @return IPage<FlowModelPageVO>
     */
    IPage<FlowModelPageVO> page(FlowModelSearchDTO entity);

    /**
     * 通过主键端查找.
     *
     * @param id 主键
     * @return FlowModelExtVO
     */
    FlowModelExtVO getDetail(Long id);
}
