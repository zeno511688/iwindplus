/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.mgt.domain.dto.system.ServerApiDTO;
import com.iwindplus.mgt.domain.dto.system.ServerApiSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ServerApiBaseVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiGroupVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiPageVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiVO;
import java.util.List;

/**
 * 服务API业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface ServerApiService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(ServerApiDTO entity);

    /**
     * 批量添加或编辑.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveOrEditBatch(List<ServerApiDTO> entities);

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
    boolean edit(ServerApiDTO entity);

    /**
     * 编辑设为隐藏.
     *
     * @param id       主键
     * @param hideFlag 是否隐藏
     * @return boolean
     */
    boolean editHideFlag(Long id, Boolean hideFlag);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<ServerApiPageVO>
     */
    IPage<ServerApiPageVO> page(ServerApiSearchDTO entity);

    /**
     * 获取所有API并分组.
     *
     * @return List<ServerApiGroupVO>
     */
    List<ServerApiGroupVO> listApiGroup();

    /**
     * 获取所有服务API.
     *
     * @return List<ServerApiBaseVO>
     */
    List<ServerApiBaseVO> listApi();

    /**
     * 详情.
     *
     * @param id 主键
     * @return ServerApiVO
     */
    ServerApiVO getDetail(Long id);

}
