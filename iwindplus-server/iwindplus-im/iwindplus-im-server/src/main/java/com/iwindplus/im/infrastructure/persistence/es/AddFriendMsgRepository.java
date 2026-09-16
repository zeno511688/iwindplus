/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.persistence.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.es.service.EsBaseService;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.im.application.query.vo.AddFriendMsgVO;
import com.iwindplus.im.common.enums.SendStatusEnum;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 加好友消息查询仓储接口类.
 *
 * @author zengdegui
 * @since 2026/09/15 10:50
 */
@Repository
public class AddFriendMsgRepository extends EsBaseServiceImpl<AddFriendMsgDO> implements EsBaseService<AddFriendMsgDO> {

    /**
     * 查询未接收到的.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return List<AddFriendMsgVO>
     */
    public List<AddFriendMsgVO> listByUnSendSuccess(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<AddFriendMsgDO> wrapper = EsWrappers.<AddFriendMsgDO>lambdaQuery()
            .eq(AddFriendMsgDO::getOrgId, orgId)
            .eq(AddFriendMsgDO::getReceiverId, userId)
            .ne(AddFriendMsgDO::getSendStatus, SendStatusEnum.SUCCESS);
        final List<AddFriendMsgDO> list = super.list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, AddFriendMsgVO.class);
    }
}
