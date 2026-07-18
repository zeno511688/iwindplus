/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.domain.vo.power.OrgBaseCheckedVO;
import com.iwindplus.mgt.domain.vo.power.OrgVO;
import com.iwindplus.mgt.server.dal.model.power.OrgDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组织数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Mapper
public interface OrgMapper extends MPJBaseMapper<OrgDO> {

    /**
     * 用户所属组织（标记选中的）.
     *
     * @param userId 用户主键
     * @return List<OrgBaseCheckedVO>
     */
    List<OrgBaseCheckedVO> selectListByUserId(@Param("userId") Long userId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return OrgVO
     */
    OrgVO selectDetailById(@Param("id") Long id);
}
