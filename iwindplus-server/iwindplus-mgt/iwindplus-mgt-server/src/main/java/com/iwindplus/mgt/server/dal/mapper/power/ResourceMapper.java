/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.domain.enums.ResourceTypeEnum;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourceExtendVO;
import com.iwindplus.mgt.server.dal.model.power.ResourceDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 资源数据访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface ResourceMapper extends MPJBaseMapper<ResourceDO> {

    /**
     * 查询启用的.
     *
     * @return List<ResourceBaseCheckedVO>
     */
    List<ResourceBaseCheckedVO> selectListEnabled();

    /**
     * 通过角色主键查询选中的.
     *
     * @param orgId  组织主键
     * @param roleId 角色主键
     * @return List<ResourceBaseCheckedVO>
     */
    List<ResourceBaseCheckedVO> selectListByRoleId(@Param("orgId") Long orgId, @Param("roleId") Long roleId);

    /**
     * 通过用户主键查询选中的.
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param types         类型集合（可选）
     * @param requestMethod 请求方式（可选）
     * @param apiUrl        API路径（可选）
     * @return List<ResourceBaseExtendVO>
     */
    List<ResourceBaseExtendVO> selectListCheckedByUserId(@Param("orgId") Long orgId, @Param("userId") Long userId,
        @Param(Constants.LIST) List<ResourceTypeEnum> types, @Param("requestMethod") String requestMethod, @Param("apiUrl") String apiUrl);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResourceExtendVO
     */
    ResourceExtendVO selectDetailById(@Param("id") Long id);

}