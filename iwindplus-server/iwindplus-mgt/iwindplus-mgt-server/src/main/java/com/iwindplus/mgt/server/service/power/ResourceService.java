/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.power.ResourceEditDTO;
import com.iwindplus.mgt.domain.dto.power.ResourceSaveDTO;
import com.iwindplus.mgt.domain.dto.power.ResourceSearchDTO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import com.iwindplus.mgt.domain.vo.power.ResourceExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourcePageVO;
import java.util.List;

/**
 * 资源业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface ResourceService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(ResourceSaveDTO entity);

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
    boolean edit(ResourceEditDTO entity);

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
     * @return IPage<ResourcePageVO>
     */
    IPage<ResourcePageVO> page(ResourceSearchDTO entity);

    /**
     * 用户按钮权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<ResourceBaseVO>
     */
    List<ResourceBaseVO> listButtonCheckedByUserId(Long orgId, Long userId);

    /**
     * 用户API权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<ResourceBaseExtendVO>
     */
    List<ResourceBaseExtendVO> listApiCheckedByUserId(Long orgId, Long userId);

    /**
     * 获取所有资源.
     *
     * @return List<ResourceBaseExtendVO>
     */
    List<ResourceBaseExtendVO> listAll();

    /**
     * 校验用户API权限.
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param requestMethod 请求方式
     * @param apiUrl        API路径
     * @return Boolean
     */
    Boolean checkApiByUserId(Long orgId, Long userId, String requestMethod, String apiUrl);

    /**
     * 详情（扩展）.
     *
     * @param id 主键
     * @return ResourceExtendVO
     */
    ResourceExtendVO getDetailExtend(Long id);

}
