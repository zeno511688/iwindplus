/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSearchDTO;
import com.iwindplus.mgt.domain.enums.BindTypeEnum;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantVO;
import com.iwindplus.mgt.server.dal.model.system.ThirdBindGrantDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第三方绑定授权数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Mapper
public interface ThirdBindGrantMapper extends MPJBaseMapper<ThirdBindGrantDO> {

    /**
     * 真实删除.
     *
     * @param ids 主键集合
     * @return int
     */
    int deleteByIds(List<Long> ids);

    /**
     * 通过用户主键真实删除.
     *
     * @param userIds 用户主键集合
     * @return int
     */
    int deleteByUserIds(List<Long> userIds);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ThirdBindGrantVO>
     */
    IPage<ThirdBindGrantVO> selectPageByCondition(PageDTO<ThirdBindGrantDO> page, @Param(Constants.WRAPPER) ThirdBindGrantSearchDTO entity);

    /**
     * 通过第三方用户唯一标识查找.
     *
     * @param openid  用户唯一标识.
     * @param unionId 用户在开放平台的唯一标识符
     * @param type    绑定类型
     * @return ThirdBindGrantVO
     */
    ThirdBindGrantVO selectByOpenId(@Param("openid") String openid, @Param("unionId") String unionId, @Param("type") BindTypeEnum type);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ThirdBindGrantVO
     */
    ThirdBindGrantVO selectDetailById(@Param("id") Long id);
}
