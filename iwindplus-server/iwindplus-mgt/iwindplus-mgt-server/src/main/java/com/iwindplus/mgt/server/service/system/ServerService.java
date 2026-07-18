/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.system.ServerDTO;
import com.iwindplus.mgt.domain.dto.system.ServerSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ServerBaseVO;
import com.iwindplus.mgt.domain.vo.system.ServerPageVO;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
import com.iwindplus.mgt.domain.vo.system.ServerVO;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 服务业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface ServerService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(ServerDTO entity);

    /**
     * 批量添加或编辑.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveOrEditBatch(@RequestBody List<ServerDTO> entities);

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
    boolean edit(ServerDTO entity);

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
     * @return IPage<ServerPageVO>
     */
    IPage<ServerPageVO> page(ServerSearchDTO entity);

    /**
     * 所有服务路由.
     *
     * @return List<ServerRouteDefinitionVO>
     */
    List<ServerRouteDefinitionVO> listRouteDefinition();

    /**
     * 查询启用的.
     *
     * @return List<ServerBaseVO>
     */
    List<ServerBaseVO> listEnabled();

    /**
     * 详情.
     *
     * @param id 主键
     * @return ServerVO
     */
    ServerVO getDetail(Long id);

    /**
     * 根据路由ID查询主键.
     *
     * @param routeId 路由ID
     * @return Long
     */
    Long getIdByRouteId(String routeId);

    /**
     * 刷新.
     */
    void flush();
}
