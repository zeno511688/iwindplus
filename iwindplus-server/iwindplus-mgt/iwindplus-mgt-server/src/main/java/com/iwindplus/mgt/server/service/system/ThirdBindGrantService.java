/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSaveEditDTO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantSearchDTO;
import com.iwindplus.mgt.domain.dto.system.ThirdBindGrantUserDTO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantVO;
import java.util.List;

/**
 * 第三方绑定授权业务层接口类.
 *
 * @author zengdegui
 * @since 2019/6/17
 */
public interface ThirdBindGrantService {
    /**
     * 绑定openid.
     *
     * @param entity 对象
     * @return ThirdBindGrantResultVO
     */
    ThirdBindGrantResultVO saveOrEdit(ThirdBindGrantSaveEditDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 通过用户主键真实删除.
     *
     * @param userIds 用户主键集合
     * @return boolean
     */
    boolean removeByUserIds(List<Long> userIds);

    /**
     * 绑定用户.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean editUser(ThirdBindGrantUserDTO entity);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<ThirdBindGrantVO>
     */
    IPage<ThirdBindGrantVO> page(ThirdBindGrantSearchDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ThirdBindGrantVO
     */
    ThirdBindGrantVO getDetail(Long id);
}
