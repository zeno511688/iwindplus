/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.im.domain.dto.DirectMsgDTO;
import com.iwindplus.im.domain.dto.DirectMsgSearchDTO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.vo.DirectMsgPageVO;
import com.iwindplus.im.domain.vo.DirectMsgVO;
import com.iwindplus.im.server.dal.model.DirectMsgDO;
import java.util.List;

/**
 * 直发消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface DirectMsgService extends EsBaseService<DirectMsgDO> {
    /**
     * 添加.
     *
     * @param entity   对象
     * @return boolean
     */
    boolean save(DirectMsgDTO entity);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(DirectMsgDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<String> ids);

    /**
     * 编辑消息状态.
     *
     * @param id        主键
     * @param msgStatus 消息状态
     * @return boolean
     */
    boolean editMsgStatus(String id, MsgStatusEnum msgStatus);

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<DirectMsgPageVO>
     */
    IPage<DirectMsgPageVO> page(PageDTO<DirectMsgDO> page, DirectMsgSearchDTO entity);

    /**
     * 查询未接收到的.
     *
     * @param userId   用户主键
     * @param orgId  组织主键
     * @return List<DirectMsgVO>
     */
    List<DirectMsgVO> listByUnSendSuccess(Long userId, Long orgId);

    /**
     * 详情.
     *
     * @param id      主键
     * @param ossTplCode 对象存储模板配置编码
     * @return DirectMsgVO
     */
    DirectMsgVO getDetail(String id, String ossTplCode);
}
