/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.OperationLogDTO;
import com.iwindplus.log.domain.dto.OperationLogNewestDTO;
import com.iwindplus.log.domain.dto.OperationLogSearchDTO;
import com.iwindplus.log.domain.vo.OperationLogExtendVO;
import com.iwindplus.log.domain.vo.OperationLogPageVO;
import com.iwindplus.log.server.dal.model.OperationLogDO;
import java.util.List;

/**
 * 操作日志业务层接口类.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
public interface OperationLogService extends EsBaseService<OperationLogDO> {

    /**
     * 保存
     *
     * @param entity    对象
     * @return boolean
     */
    boolean save(OperationLogDTO entity);

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
     * @return IPage<OperationLogPageVO>
     */
    IPage<OperationLogPageVO> page(OperationLogSearchDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return OperationLogExtendVO
     */
    OperationLogExtendVO getDetail(String id);

    /**
     * 根据条件获取最新数据.
     *
     * @param entity 对象
     * @return OperationLogExtendVO
     */
    OperationLogExtendVO getNewestByCondition(OperationLogNewestDTO entity);
}
