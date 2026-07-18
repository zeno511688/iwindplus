/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.log.domain.dto.SmsCaptchaLogDTO;
import com.iwindplus.log.domain.dto.SmsCaptchaLogSearchDTO;
import com.iwindplus.log.domain.dto.SmsSendValidDTO;
import com.iwindplus.log.domain.vo.SmsCaptchaLogPageVO;
import com.iwindplus.log.domain.vo.SmsCaptchaLogVO;
import com.iwindplus.log.server.dal.model.SmsCaptchaLogDO;
import java.util.List;

/**
 * 短信验证码日志业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface SmsCaptchaLogService extends EsBaseService<SmsCaptchaLogDO> {

    /**
     * 添加.
     *
     * @param entity   对象
     * @return String
     */
    String save(SmsCaptchaLogDTO entity);

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
     * @return IPage<SmsCaptchaLogPageVO>
     */
    IPage<SmsCaptchaLogPageVO> page(SmsCaptchaLogSearchDTO entity);

    /**
     * 校验是否可以发送.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean checkCanSend(SmsSendValidDTO entity);

    /**
     * 查找详情.
     *
     * @param id 主键
     * @return SmsCaptchaLogVO
     */
    SmsCaptchaLogVO getDetail(String id);

    /**
     * 校验验证码（手机）.
     *
     * @param tplCode 模板配置编码
     * @param mobile  手机
     * @param captcha 验证码
     * @return boolean
     */
    boolean validate(String tplCode, String mobile, String captcha);

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
