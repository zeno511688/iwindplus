/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.im.application.service.dto.SysNoticeMsgDTO;
import com.iwindplus.im.infrastructure.persistence.es.SysNoticeMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.SysNoticeMsgRepository;
import com.iwindplus.mgt.api.upms.vo.UserExtendVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统通知消息业务层接口类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeMsgApplicationService {

    private final SysNoticeMsgRepository sysNoticeMsgRepository;
    private final UserClient userClient;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean save(SysNoticeMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        List<Long> ids = List.of(userId);
        final UserExtendVO data = Optional.ofNullable(this.userClient.listExtendByIds(ids)).map(ResultVO::getBizData).map(m -> m.get(0))
            .orElse(null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        String avatar = data.getAvatar();
        String nickName = data.getNickName();
        entity.setSeq(this.sysNoticeMsgRepository.getNextSeq(orgId));
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);
        entity.setOrgId(orgId);
        final SysNoticeMsgDO model = BeanUtil.copyProperties(entity, SysNoticeMsgDO.class);
        this.sysNoticeMsgRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    public boolean removeByIds(List<String> ids) {
        List<SysNoticeMsgDO> list = this.sysNoticeMsgRepository.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.sysNoticeMsgRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean edit(SysNoticeMsgDTO entity) {
        SysNoticeMsgDO data = this.sysNoticeMsgRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final SysNoticeMsgDO model = BeanUtil.copyProperties(entity, SysNoticeMsgDO.class);
        return this.sysNoticeMsgRepository.updateById(model);
    }
}
