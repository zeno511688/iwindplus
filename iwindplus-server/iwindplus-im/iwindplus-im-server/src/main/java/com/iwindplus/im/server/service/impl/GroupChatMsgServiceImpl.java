/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.im.domain.dto.GroupChatMsgDTO;
import com.iwindplus.im.domain.dto.GroupChatMsgSearchDTO;
import com.iwindplus.im.domain.enums.ImCodeEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.GroupChatMsgPageVO;
import com.iwindplus.im.domain.vo.GroupChatMsgVO;
import com.iwindplus.im.server.dal.model.ChatGroupDO;
import com.iwindplus.im.server.dal.model.GroupChatMsgDO;
import com.iwindplus.im.server.dal.repository.ChatGroupRepository;
import com.iwindplus.im.server.service.GroupChatMsgService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.setup.client.OssClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 群聊消息业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class GroupChatMsgServiceImpl extends EsBaseServiceImpl<GroupChatMsgDO> implements GroupChatMsgService {

    private final ChatGroupRepository chatGroupRepository;
    private final OssClient ossClient;
    private final UserClient userClient;

    @Override
    public boolean save(GroupChatMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        List<Long> ids = Arrays.asList(userId);
        final UserExtendVO data = Optional.ofNullable(this.userClient.listExtendByIds(ids)).map(ResultVO::getBizData).map(m -> m.get(0)).orElse(null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        String avatar = data.getAvatar();
        String nickName = data.getNickName();
        final ChatGroupDO result = this.chatGroupRepository.getById(entity.getChatGroupId());
        if (Objects.isNull(result)) {
            throw new BizException(ImCodeEnum.CHAT_GROUP_NOT_EXIST);
        }
        entity.setSeq(this.getNextSeq(orgId, entity.getChatGroupId()));
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);
        entity.setOrgId(orgId);
        final GroupChatMsgDO model = BeanUtil.copyProperties(entity, GroupChatMsgDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(GroupChatMsgDTO entity) {
        GroupChatMsgDO data = super.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final GroupChatMsgDO model = BeanUtil.copyProperties(entity, GroupChatMsgDO.class);
        return super.updateById(model);
    }

    @Override
    public boolean removeByChatGroupIds(List<Long> chatGroupIds) {
        super.removeByIds(chatGroupIds, false);
        return Boolean.TRUE;
    }

    @Override
    public IPage<GroupChatMsgPageVO> page(PageDTO<GroupChatMsgDO> page, GroupChatMsgSearchDTO entity) {
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<GroupChatMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(GroupChatMsgDO::getOrgId, orgId)
            .eq(GroupChatMsgDO::getChatGroupId, entity.getChatGroupId())
            .eq(GroupChatMsgDO::getSendStatus, entity.getSendStatus());
        if (Objects.nonNull(entity.getSenderId())) {
            wrapper.eq(GroupChatMsgDO::getSenderId, entity.getSenderId());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<GroupChatMsgDO> modelPage = super.page(page, wrapper);
        final IPage<GroupChatMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, GroupChatMsgPageVO.class));
        this.buildUserPic(result.getRecords(), entity.getMgtOssTplCode());
        return result;
    }

    @Override
    public GroupChatMsgVO getDetail(String id, String ossTplCode) {
        GroupChatMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getSenderAvatar())) {
            relativePaths.add(data.getSenderAvatar());
        }
        List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
        final GroupChatMsgVO result = BeanUtil.copyProperties(data, GroupChatMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId       组织主键
     * @param chatGroupId 聊天群主键
     * @return Integer
     */
    private Integer getNextSeq(Long orgId, Long chatGroupId) {
        EsLambdaQueryWrapper<GroupChatMsgDO> wrapper = new EsLambdaQueryWrapper<>();

        wrapper
            .eq(GroupChatMsgDO::getOrgId, orgId)
            .eq(GroupChatMsgDO::getChatGroupId, chatGroupId)
            .max(GroupChatMsgDO::getSeq)
            .limit(0);

        SearchHits<GroupChatMsgDO> result =
            getOperations().search(
                wrapper.build(),
                GroupChatMsgDO.class
            );

        return parseSeq(result.getAggregations().aggregations());
    }

    private int parseSeq(Object aggregations) {
        if (aggregations instanceof List<?> aggregationList) {
            return aggregationList.stream()
                .filter(ElasticsearchAggregation.class::isInstance)
                .map(ElasticsearchAggregation.class::cast)
                .map(ElasticsearchAggregation::aggregation)
                .map(Aggregation::getAggregate)
                .map(Aggregate::max)
                .map(max -> max != null ? max.value() : null)
                .filter(Objects::nonNull)
                .findFirst()
                .map(i -> i.intValue() + 1)
                .orElse(1);
        }
        return 1;
    }

    private void buildUserPic(List<GroupChatMsgPageVO> records, String ossTplCode) {
        if (CollUtil.isNotEmpty(records) && CharSequenceUtil.isNotBlank(ossTplCode)) {
            List<String> senderAvatars = records.stream().filter(Objects::nonNull)
                .filter(m -> CharSequenceUtil.isNotBlank(m.getSenderAvatar()))
                .map(GroupChatMsgPageVO::getSenderAvatar).distinct().collect(Collectors.toCollection(ArrayList::new));
            List<String> relativePaths = Lists.newArrayList();
            if (CollUtil.isNotEmpty(senderAvatars)) {
                relativePaths.addAll(senderAvatars);
            }
            List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
            records.forEach(m -> this.buildUserInfo(m, filePaths));
        }
    }

    private void buildUserInfo(GroupChatMsgPageVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
        });
    }

    private void buildUserInfo(GroupChatMsgVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
        });
    }
}
