package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.im.domain.dto.MsgIntegrationDetailDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.DirectMsgVO;
import com.iwindplus.im.domain.vo.MsgIntegrationVO;
import com.iwindplus.im.domain.vo.SysNoticeMsgVO;
import com.iwindplus.im.server.dal.model.DirectMsgDO;
import com.iwindplus.im.server.dal.model.SysNoticeMsgDO;
import com.iwindplus.im.server.service.DirectMsgService;
import com.iwindplus.im.server.service.MsgIntegrationService;
import com.iwindplus.im.server.service.SysNoticeMsgService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息集成业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/03/23 15:15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsgIntegrationServiceImpl implements MsgIntegrationService {

    private final DirectMsgService directMsgService;
    private final SysNoticeMsgService sysNoticeMsgService;

    @Override
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

                final DirectMsgDO data = directMsgService.getById(id);
                DirectMsgVO vo = BeanUtil.copyProperties(data, DirectMsgVO.class);
                if (MsgStatusEnum.READ.equals(vo.getMsgStatus())) {
                    result.setReadMsgs(List.of(vo));
                } else {
                    result.setUnReadMsgs(List.of(vo));
                }

            } else if (CommandEnum.SYS_NOTICE_MSG.equals(command)) {
                final SysNoticeMsgDO data = sysNoticeMsgService.getById(id);
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

        EsLambdaQueryWrapper<DirectMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(DirectMsgDO::getOrgId, orgId)
            .orderByDesc(DirectMsgDO::getModifiedTimestamp)
            .limit(limit);
        if (userId != null) {
            wrapper.eq(DirectMsgDO::getReceiverId, userId);
        }
        if (status != null) {
            wrapper.eq(DirectMsgDO::getSendStatus, SendStatusEnum.SUCCESS)
                .eq(DirectMsgDO::getMsgStatus, status);
        }

        List<DirectMsgDO> list = directMsgService.list(wrapper);

        setResult(list, DirectMsgVO.class, setter, countSetter);
    }

    private void fetchSysMsg(
        Long orgId,
        int limit,
        Consumer<List<SysNoticeMsgVO>> setter,
        Consumer<Integer> countSetter) {

        EsLambdaQueryWrapper<SysNoticeMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(SysNoticeMsgDO::getOrgId, orgId)
            .orderByDesc(SysNoticeMsgDO::getModifiedTimestamp)
            .limit(limit);

        List<?> list = sysNoticeMsgService.list(wrapper);

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