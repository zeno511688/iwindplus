/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.setup.domain.dto.MailTplEditDTO;
import com.iwindplus.setup.domain.dto.MailTplSaveDTO;
import com.iwindplus.setup.domain.dto.MailTplSearchDTO;
import com.iwindplus.setup.domain.vo.MailTplBaseVO;
import com.iwindplus.setup.domain.vo.MailTplPageVO;
import com.iwindplus.setup.domain.vo.MailTplVO;
import com.iwindplus.setup.server.dal.model.MailTplDO;
import java.util.List;

/**
 * 邮箱模板业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface MailTplService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(MailTplSaveDTO entity);

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
    boolean edit(MailTplEditDTO entity);

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
     * @return IPage<MailTplPageVO>
     */
    IPage<MailTplPageVO> page(PageDTO<MailTplDO> page, MailTplSearchDTO entity);

    /**
     * 通过编码查找.
     *
     * @param code 编码
     * @return MailTplVO
     */
    MailTplVO getByCode(String code);

    /**
     * 详情.
     *
     * @param id 主键
     * @return MailTplVO
     */
    MailTplVO getDetail(Long id);

    /**
     * 启用的列表.
     *
     * @return List<MailTplBaseVO>
     */
    List<MailTplBaseVO> listEnabled();
}
