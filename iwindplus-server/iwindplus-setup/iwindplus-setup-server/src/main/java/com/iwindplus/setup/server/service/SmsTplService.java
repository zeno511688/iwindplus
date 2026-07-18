/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.setup.domain.dto.SmsTplEditDTO;
import com.iwindplus.setup.domain.dto.SmsTplSaveDTO;
import com.iwindplus.setup.domain.dto.SmsTplSearchDTO;
import com.iwindplus.setup.domain.vo.SmsTplPageVO;
import com.iwindplus.setup.domain.vo.SmsTplVO;
import com.iwindplus.setup.server.dal.model.SmsTplDO;
import java.util.List;

/**
 * 短信模板业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface SmsTplService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(SmsTplSaveDTO entity);

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
    boolean edit(SmsTplEditDTO entity);

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
     * @return IPage<SmsTplPageVO>
     */
    IPage<SmsTplPageVO> page(PageDTO<SmsTplDO> page, SmsTplSearchDTO entity);

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return SmsTplVO
     */
    SmsTplVO getByCode(String code);

    /**
     * 详情.
     *
     * @param id 主键
     * @return SmsTplVO
     */
    SmsTplVO getDetail(Long id);

}
