/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.application.query;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.im.application.query.dto.MsgIntegrationDetailDTO;
import com.iwindplus.im.application.query.vo.DirectMsgVO;
import com.iwindplus.im.application.query.vo.MsgIntegrationVO;
import com.iwindplus.im.application.query.vo.SysNoticeMsgVO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.common.enums.MsgStatusEnum;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.persistence.es.DirectMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.DirectMsgRepository;
import com.iwindplus.im.infrastructure.persistence.es.SysNoticeMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.SysNoticeMsgRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息集成查询业务层.
 *
 * @author zengdegui
 * @since 2023/12/04 23:22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsgIntegrationQueryService {

    private final DirectMsgRepository directMsgRepository;
    private final SysNoticeMsgRepository sysNoticeMsgRepository;

    /**
     * 获取消息.
     *
     * @param entity 对象
     * @return MsgIntegrationVO
     */
    public MsgIntegrationVO getMsg(MsgIntegrationDetailDTO entity) {
        Integer limit = Optional.ofNullable(entity.getSize())
            .orElse(CommonConstant.NumberConstant.NUMBER_TEN);

        MsgIntegrationVO result = new MsgIntegrationVO();

        String id = entity.getId();
        CommandEnum command = entity.getCommand();
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();

        if (Objects.nonNull(command) && CharSequenceUtil.isNotBlank(id)) {
            if (CommandEnum.DIRECT_MSG.equals(command)) {

                final DirectMsgDO data = directMsgRepository.getById(id);
                DirectMsgVO vo = BeanUtil.copyProperties(data, DirectMsgVO.class);
                if (MsgStatusEnum.READ.equals(vo.getMsgStatus())) {
                    result.setReadMsgs(List.of(vo));
                } else {
                    result.setUnReadMsgs(List.of(vo));
                }

            } else if (CommandEnum.SYS_NOTICE_MSG.equals(command)) {
                final SysNoticeMsgDO data = sysNoticeMsgRepository.getById(id);
                result.setSysNoticeMsgs(
                    List.of(BeanUtil.copyProperties(data, SysNoticeMsgVO.class))
                );
            }

            return result;
        }

        fetchDirectMsg(orgId, userId, MsgStatusEnum.UN_READ, limit,
            result::setUnReadMsgs, result::setUnReadMsgCount);

        fetchDirectMsg(orgId, userId, MsgStatusEnum.READ, limit,
            result::setReadMsgs, result::setReadMsgCount);

        fetchSysMsg(orgId, limit,
            result::setSysNoticeMsgs, result::setSysNoticeMsgCount);

        fetchDirectMsg(orgId, userId, MsgStatusEnum.RECYCLED, limit,
            result::setRecycleMsgs, result::setRecycleMsgCount);

        return result;
    }

    private void fetchDirectMsg(
        Long orgId,
        Long userId,
        MsgStatusEnum status,
        int limit,
        Consumer<List<DirectMsgVO>> setter,
        Consumer<Integer> countSetter) {

        EsLambdaQueryWrapper<DirectMsgDO> wrapper = EsWrappers.<DirectMsgDO>lambdaQuery()
            .eq(DirectMsgDO::getOrgId, orgId)
            .orderByDesc(DirectMsgDO::getModifiedTimestamp)
            .limit(limit);
        if (userId != null) {
            wrapper.eq(DirectMsgDO::getReceiverId, userId);
        }
        if (status != null) {
            wrapper.eq(DirectMsgDO::getSendStatus, SendStatusEnum.SUCCESS)
                .eq(DirectMsgDO::getMsgStatus, status);
        }

        List<DirectMsgDO> list = directMsgRepository.list(wrapper);

        setResult(list, DirectMsgVO.class, setter, countSetter);
    }

    private void fetchSysMsg(
        Long orgId,
        int limit,
        Consumer<List<SysNoticeMsgVO>> setter,
        Consumer<Integer> countSetter) {

        EsLambdaQueryWrapper<SysNoticeMsgDO> wrapper = EsWrappers.<SysNoticeMsgDO>lambdaQuery()
            .eq(SysNoticeMsgDO::getOrgId, orgId)
            .orderByDesc(SysNoticeMsgDO::getModifiedTimestamp)
            .limit(limit);

        List<?> list = sysNoticeMsgRepository.list(wrapper);

        setResult(list, SysNoticeMsgVO.class, setter, countSetter);
    }

    private <VO> void setResult(
        List<?> list,
        Class<VO> voClass,
        Consumer<List<VO>> setter,
        Consumer<Integer> countSetter) {

        if (CollUtil.isEmpty(list)) {
            setter.accept(Collections.emptyList());
            countSetter.accept(0);
            return;
        }

        List<VO> voList = BeanUtil.copyToList(list, voClass);

        setter.accept(voList);
        countSetter.accept(voList.size());
    }

}
