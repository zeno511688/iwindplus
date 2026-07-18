/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.domain.dto.LoginLogSearchDTO;
import com.iwindplus.log.domain.vo.LoginLogExtendVO;
import com.iwindplus.log.domain.vo.LoginLogPageVO;
import com.iwindplus.log.domain.vo.LoginLogVO;
import com.iwindplus.log.server.dal.model.LoginLogDO;
import java.util.List;

/**
 * 登录日志业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface LoginLogService extends EsBaseService<LoginLogDO> {

    /**
     * 保存
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(LoginLogDTO entity);

    /**
     * 保存
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<LoginLogDTO> entities);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<String> ids);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<LoginLogPageVO>
     */
    IPage<LoginLogPageVO> page(LoginLogSearchDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return LoginLogExtendVO
     */
    LoginLogExtendVO getDetail(String id);

    /**
     * 获取最新登录信息.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return LoginLogVO
     */
    LoginLogVO getLoginInfo(Long userId, Long orgId);
}
