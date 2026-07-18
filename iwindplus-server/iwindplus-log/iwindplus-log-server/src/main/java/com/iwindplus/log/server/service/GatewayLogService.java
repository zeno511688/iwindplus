/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.domain.dto.GatewayLogSearchDTO;
import com.iwindplus.log.domain.vo.GatewayLogExtendVO;
import com.iwindplus.log.domain.vo.GatewayLogPageVO;
import com.iwindplus.log.server.dal.model.GatewayLogDO;
import java.util.List;

/**
 * 网关日志业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface GatewayLogService extends EsBaseService<GatewayLogDO> {

    /**
     * 保存
     *
     * @param entities  对象集合
     * @return boolean
     */
    boolean saveBatch(List<GatewayLogDTO> entities);

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
     * @return IPage<GatewayLogPageVO>
     */
    IPage<GatewayLogPageVO> page(GatewayLogSearchDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return GatewayLogExtendVO
     */
    GatewayLogExtendVO getDetail(String id);
}
