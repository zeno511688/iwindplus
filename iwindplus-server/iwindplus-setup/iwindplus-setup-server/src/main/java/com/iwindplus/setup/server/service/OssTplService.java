/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.setup.domain.dto.OssTplEditDTO;
import com.iwindplus.setup.domain.dto.OssTplSaveDTO;
import com.iwindplus.setup.domain.dto.OssTplSearchDTO;
import com.iwindplus.setup.domain.vo.OssTplPageVO;
import com.iwindplus.setup.domain.vo.OssTplVO;
import com.iwindplus.setup.server.dal.model.OssTplDO;
import java.util.List;

/**
 * 对象存储模板业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface OssTplService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(OssTplSaveDTO entity);

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
    boolean edit(OssTplEditDTO entity);

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
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<OssTplPageVO>
     */
    IPage<OssTplPageVO> page(PageDTO<OssTplDO> page, OssTplSearchDTO entity);

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return OssTplVO
     */
    OssTplVO getByCode(String code);

    /**
     * 详情.
     *
     * @param id 主键
     * @return OssTplVO
     */
    OssTplVO getDetail(Long id);

}
