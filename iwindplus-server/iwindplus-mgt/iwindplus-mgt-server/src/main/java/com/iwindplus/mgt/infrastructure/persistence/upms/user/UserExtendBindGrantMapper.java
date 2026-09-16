/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.upms.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.application.query.upms.user.dto.UserExtendBindGrantSearchDTO;
import com.iwindplus.mgt.common.enums.BindTypeEnum;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantPageVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户扩展绑定授权数据访问层接口类.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Mapper
public interface UserExtendBindGrantMapper extends MPJBaseMapper<UserExtendBindGrantDO> {

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
     * @return IPage<UserExtendBindGrantPageVO>
     */
    IPage<UserExtendBindGrantPageVO> selectPageByCondition(PageDTO<UserExtendBindGrantDO> page, @Param(Constants.WRAPPER) UserExtendBindGrantSearchDTO entity);

    /**
     * 通过用户唯一标识查找.
     *
     * @param openid  用户唯一标识.
     * @param unionId 用户在开放平台的唯一标识符
     * @param type    绑定类型
     * @return UserExtendBindGrantVO
     */
    UserExtendBindGrantVO selectByOpenId(@Param("openid") String openid, @Param("unionId") String unionId, @Param("type") BindTypeEnum type);

    /**
     * 详情.
     *
     * @param id 主键
     * @return UserExtendBindGrantVO
     */
    UserExtendBindGrantVO selectDetailById(@Param("id") Long id);
}
