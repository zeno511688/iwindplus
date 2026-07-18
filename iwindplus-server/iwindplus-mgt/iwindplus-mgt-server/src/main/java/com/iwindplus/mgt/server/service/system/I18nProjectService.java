/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.BaseVO;
import com.iwindplus.mgt.domain.dto.system.I18nProjectExtendDTO;
import com.iwindplus.mgt.domain.dto.system.I18nProjectSearchDTO;
import com.iwindplus.mgt.domain.vo.system.I18nProjectExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nProjectPageVO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 国际化项目业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface I18nProjectService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(I18nProjectExtendDTO entity);

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
    boolean edit(I18nProjectExtendDTO entity);

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
     * @return IPage<I18nProjectPageVO>
     */
    IPage<I18nProjectPageVO> page(I18nProjectSearchDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return I18nProjectExtendVO
     */
    I18nProjectExtendVO getDetail(Long id);

    /**
     * 获取启用状态的列表.
     *
     * @return List<BaseVO>
     */
    List<BaseVO> listEnabled();

    /**
     * 推送数据至Nacos.
     *
     * @param id 主键
     * @return boolean
     */
    boolean pushData(Long id);

    /**
     * 下载.
     *
     * @param id      主键
     * @param response 响应
     */
    void download(Long id, HttpServletResponse response);
}
