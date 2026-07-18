/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.system.I18nMsgDTO;
import com.iwindplus.mgt.domain.dto.system.I18nMsgSearchDTO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgPageVO;
import java.util.List;

/**
 * 国际化消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface I18nMsgService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(I18nMsgDTO entity);

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
    boolean edit(I18nMsgDTO entity);

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
     * @return IPage<I18nMsgPageVO>
     */
    IPage<I18nMsgPageVO> page(I18nMsgSearchDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return I18nMsgExtendVO
     */
    I18nMsgExtendVO getDetail(Long id);

}
