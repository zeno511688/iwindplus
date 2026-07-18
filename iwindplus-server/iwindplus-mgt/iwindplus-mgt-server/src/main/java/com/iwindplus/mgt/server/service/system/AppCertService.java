/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.mgt.domain.dto.system.AppCertDTO;
import com.iwindplus.mgt.domain.dto.system.AppCertSearchDTO;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.mgt.domain.vo.system.AppCertBaseVO;
import com.iwindplus.mgt.domain.vo.system.AppCertPageVO;
import com.iwindplus.mgt.domain.vo.system.AppCertVO;
import java.util.List;

/**
 * 应用凭证业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface AppCertService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(AppCertDTO entity);

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
    boolean edit(AppCertDTO entity);

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
     * 重置密钥.
     *
     * @param id 主键
     * @return AppCertBaseVO
     */
    AppCertBaseVO editSecret(Long id);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<AppCertPageVO>
     */
    IPage<AppCertPageVO> page(AppCertSearchDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return AppCertVO
     */
    AppCertVO getDetail(Long id);

    /**
     * 通过访问key查找.
     *
     * @param accessKey   访问key
     * @param appCertType 应用凭证类型
     * @return AppSignVO
     */
    BaseSignVO getByAccessKey(String accessKey, AppCertTypeEnum appCertType);

    /**
     * 通过应用凭证类型查找.
     *
     * @param appCertType 应用凭证类型
     * @return AppSignVO
     */
    BaseSignVO getByCertType(AppCertTypeEnum appCertType);
}
