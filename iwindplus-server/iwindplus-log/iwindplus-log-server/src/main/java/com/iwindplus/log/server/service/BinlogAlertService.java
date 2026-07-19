/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import com.iwindplus.log.domain.dto.BinlogAlertSearchDTO;
import com.iwindplus.log.domain.vo.BinlogAlertPageVO;
import com.iwindplus.log.domain.vo.BinlogAlertVO;
import com.iwindplus.log.server.dal.model.BinlogAlertDO;
import java.util.List;

/**
 * binlog告警业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface BinlogAlertService extends EsBaseService<BinlogAlertDO> {

    /**
     * 保存
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(BinlogAlertDTO entity);

    /**
     * 批量保存
     *
     * @param entities  对象集合
     * @return boolean
     */
    boolean saveBatch(List<BinlogAlertDTO> entities);

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
     * @return IPage<BinlogAlertPageVO>
     */
    IPage<BinlogAlertPageVO> page(BinlogAlertSearchDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return BinlogAlertVO
     */
    BinlogAlertVO getDetail(String id);
}
