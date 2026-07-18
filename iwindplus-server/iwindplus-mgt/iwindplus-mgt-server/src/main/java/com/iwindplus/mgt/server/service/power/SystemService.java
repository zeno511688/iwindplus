/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.SystemEditDTO;
import com.iwindplus.mgt.domain.dto.power.SystemSaveDTO;
import com.iwindplus.mgt.domain.dto.power.SystemSearchDTO;
import com.iwindplus.mgt.domain.vo.power.SystemBaseVO;
import com.iwindplus.mgt.domain.vo.power.SystemExtendVO;
import com.iwindplus.mgt.domain.vo.power.SystemPageVO;
import com.iwindplus.mgt.domain.vo.power.SystemVO;
import java.util.List;

/**
 * 系统业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
public interface SystemService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(SystemSaveDTO entity);

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
    boolean edit(SystemEditDTO entity);

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
     * @return IPage<SystemPageVO>
     */
    IPage<SystemPageVO> page(SystemSearchDTO entity);

    /**
     * 查询所有启用的.
     *
     * @return List<SystemBaseVO>
     */
    List<SystemBaseVO> listByEnabled();

    /**
     * 详情.
     *
     * @param id         主键
     * @return SystemVO
     */
    SystemVO getDetail(Long id);

    /**
     * 详情（扩展）.
     *
     * @param id         主键
     * @return SystemExtendVO
     */
    SystemExtendVO getDetailExtend(Long id);

}
