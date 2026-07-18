/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.system.ClientDTO;
import com.iwindplus.mgt.domain.dto.system.ClientSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ClientBaseVO;
import com.iwindplus.mgt.domain.vo.system.ClientPageVO;
import com.iwindplus.mgt.domain.vo.system.ClientVO;
import java.util.List;

/**
 * 客户端业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface ClientService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ClientBaseVO
     */
    ClientBaseVO save(ClientDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 编辑（id必选）.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(ClientDTO entity);

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    boolean editStatus(Long id, EnableStatusEnum status);

    /**
     * 重置密钥.
     *
     * @param id 主键
     * @return ClientBaseVO
     */
    ClientBaseVO editSecret(Long id);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<ClientPageVO>
     */
    IPage<ClientPageVO> page(ClientSearchDTO entity);

    /**
     * 通过客户端id查询.
     *
     * @param clientId 客户端id
     * @return ClientVO
     */
    ClientVO getByClientId(String clientId);

    /**
     * 详情.
     *
     * @param id 主键
     * @return ClientVO
     */
    ClientVO getDetail(String id);
}
