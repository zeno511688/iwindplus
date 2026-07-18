/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.PositionEditDTO;
import com.iwindplus.mgt.domain.dto.power.PositionSaveDTO;
import com.iwindplus.mgt.domain.dto.power.PositionSearchDTO;
import com.iwindplus.mgt.domain.vo.power.PositionBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.PositionExtendVO;
import com.iwindplus.mgt.domain.vo.power.PositionPageVO;
import java.util.List;

/**
 * 职位业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface PositionService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(PositionSaveDTO entity);

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
    boolean edit(PositionEditDTO entity);

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
     * @return IPage<PositionPageVO>
     */
    IPage<PositionPageVO> page(PositionSearchDTO entity);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return PositionExtendVO
     */
    PositionExtendVO getDetailExtend(Long id);

    /**
     * 通过用户主键获取组织职位（标记选中）.
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param departmentIds 部门主键集合
     * @return List<PositionBaseCheckedVO>
     */
    List<PositionBaseCheckedVO> listByUserId(Long orgId, Long userId, List<Long> departmentIds);
}
