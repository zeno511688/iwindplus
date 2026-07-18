/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.im.domain.dto.SysNoticeMsgDTO;
import com.iwindplus.im.domain.dto.SysNoticeMsgSearchDTO;
import com.iwindplus.im.domain.vo.SysNoticeMsgPageVO;
import com.iwindplus.im.domain.vo.SysNoticeMsgVO;
import com.iwindplus.im.server.dal.model.SysNoticeMsgDO;
import java.util.List;

/**
 * 系统通知消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
public interface SysNoticeMsgService extends EsBaseService<SysNoticeMsgDO> {

    /**
     * 添加.
     *
     * @param entity   对象
     * @return boolean
     */
    boolean save(SysNoticeMsgDTO entity);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(SysNoticeMsgDTO entity);

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
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<SysNoticeMsgPageVO>
     */
    IPage<SysNoticeMsgPageVO> page(PageDTO<SysNoticeMsgDO> page, SysNoticeMsgSearchDTO entity);

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return SysNoticeMsgVO
     */
    SysNoticeMsgVO getDetail(String id, String ossTplCode);
}
