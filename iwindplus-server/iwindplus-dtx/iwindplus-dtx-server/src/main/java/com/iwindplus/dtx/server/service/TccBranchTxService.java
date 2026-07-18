/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import com.iwindplus.dtx.domain.dto.TccBranchTxSearchDTO;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import com.iwindplus.dtx.domain.vo.TccBranchTxPageVO;
import com.iwindplus.dtx.domain.vo.TccBranchTxVO;
import com.iwindplus.dtx.server.dal.model.TccBranchTxDO;
import java.util.List;

/**
 * tcc分支事务业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface TccBranchTxService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(TccBranchTxDTO entity);

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
    boolean edit(TccBranchTxDTO entity);

    /**
     * 通过主键修改状态.
     *
     * @param id   主键
     * @param from 从状态
     * @param to   到状态
     * @return boolean
     */
    boolean editStatusById(Long id, BranchTxStatusEnum from, BranchTxStatusEnum to);

    /**
     * 通过主键修改状态.
     *
     * @param id       主键
     * @param from     从状态
     * @param to       到状态
     * @param errorMsg 错误信息
     * @return boolean
     */
    boolean editStatusById(Long id, BranchTxStatusEnum from, BranchTxStatusEnum to, String errorMsg);

    /**
     * 通过主键修改状态.
     *
     * @param id       主键
     * @param fromList 从状态
     * @param to       到状态
     * @return boolean
     */
    boolean editStatusByMultiFrom(Long id, List<BranchTxStatusEnum> fromList, BranchTxStatusEnum to);

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<TccBranchTxPageVO>
     */
    IPage<TccBranchTxPageVO> page(TccBranchTxSearchDTO entity);

    /**
     * 详情.
     *
     * @param id 主键
     * @return TccBranchTxVO
     */
    TccBranchTxVO getDetail(Long id);

    /**
     * 通过全局事务ID查询.
     *
     * @param xid        全局事务ID
     * @param statusList 状态集合
     * @return List<TccBranchTxDO>
     */
    List<TccBranchTxDO> listByXid(String xid, List<BranchTxStatusEnum> statusList);
}
