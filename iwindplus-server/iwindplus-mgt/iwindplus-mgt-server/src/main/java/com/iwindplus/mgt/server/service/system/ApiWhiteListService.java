/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListDTO;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ApiWhiteListPageVO;
import com.iwindplus.mgt.domain.vo.system.ApiWhiteListVO;
import java.util.List;

/**
 * API白名单业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface ApiWhiteListService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(ApiWhiteListDTO entity);

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
    boolean edit(ApiWhiteListDTO entity);

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
     * @return IPage<ApiWhiteListPageVO>
     */
    IPage<ApiWhiteListPageVO> page(ApiWhiteListSearchDTO entity);

    /**
     * 查询所有.
     *
     * @return List<String>
     */
    List<String> listApi();

    /**
     * 详情.
     *
     * @param id 主键
     * @return ApiWhiteListVO
     */
    ApiWhiteListVO getDetail(Long id);
}
