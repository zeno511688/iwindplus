/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.MailLogDTO;
import com.iwindplus.log.domain.dto.MailLogSearchDTO;
import com.iwindplus.log.domain.vo.MailLogPageVO;
import com.iwindplus.log.domain.vo.MailLogVO;
import com.iwindplus.log.server.dal.model.MailLogDO;
import java.util.List;

/**
 * 邮箱日志业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface MailLogService extends EsBaseService<MailLogDO> {

    /**
     * 添加.
     *
     * @param entity   对象
     * @return String
     */
    String save(MailLogDTO entity);

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean edit(MailLogDTO entity);

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
     * @return IPage<MailLogPageVO>
     */
    IPage<MailLogPageVO> page(MailLogSearchDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return MailLogVO
     */
    MailLogVO getDetail(String id);
}
