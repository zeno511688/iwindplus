/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.MailCaptchaLogDTO;
import com.iwindplus.log.domain.dto.MailCaptchaLogSearchDTO;
import com.iwindplus.log.domain.dto.MailSendValidDTO;
import com.iwindplus.log.domain.vo.MailCaptchaLogPageVO;
import com.iwindplus.log.domain.vo.MailCaptchaLogVO;
import com.iwindplus.log.server.dal.model.MailCaptchaLogDO;
import java.util.List;

/**
 * 邮箱验证码日志业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface MailCaptchaLogService extends EsBaseService<MailCaptchaLogDO> {

    /**
     * 添加.
     *
     * @param entity   对象
     * @return String
     */
    String save(MailCaptchaLogDTO entity);

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<String> ids);

    /**
     * 清理过期的数据.
     *
     * @return boolean
     */
    boolean removeExpireData();

    /**
     * 列表.
     *
     * @param entity 对象
     * @return IPage<MailCaptchaLogPageVO>
     */
    IPage<MailCaptchaLogPageVO> page(MailCaptchaLogSearchDTO entity);

    /**
     * 校验是否可以发送.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean checkCanSend(MailSendValidDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return MailCaptchaLogVO
     */
    MailCaptchaLogVO getDetail(String id);

    /**
     * 校验验证码（邮箱）.
     *
     * @param tplCode 模板配置编码
     * @param mail    邮箱
     * @param captcha 验证码
     * @return boolean
     */
    boolean validate(String tplCode, String mail, String captcha);

    /**
     * 校验验证码（用户主键）.
     *
     * @param tplCode 模板配置编码
     * @param userId  用户主键
     * @param orgId   组织主键
     * @param captcha 验证码
     * @return boolean
     */
    boolean validateByUserId(String tplCode, Long userId, Long orgId, String captcha);
}
