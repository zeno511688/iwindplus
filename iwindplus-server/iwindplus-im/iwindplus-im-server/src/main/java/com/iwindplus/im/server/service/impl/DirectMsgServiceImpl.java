/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.im.domain.dto.DirectMsgDTO;
import com.iwindplus.im.domain.dto.DirectMsgSearchDTO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.DirectMsgPageVO;
import com.iwindplus.im.domain.vo.DirectMsgVO;
import com.iwindplus.im.server.dal.model.DirectMsgDO;
import com.iwindplus.im.server.service.DirectMsgService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.vo.power.UserExtendVO;
import com.iwindplus.setup.client.OssClient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.Aggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 直发消息业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DirectMsgServiceImpl extends EsBaseServiceImpl<DirectMsgDO> implements DirectMsgService {

    private final UserClient userClient;
    private final OssClient ossClient;

    @Override
    public boolean save(DirectMsgDTO entity) {
        Long userId = entity.getSenderId();
        Long orgId = entity.getOrgId();

        final Long receiverId = entity.getReceiverId();
        List<Long> ids = Arrays.asList(userId, receiverId);
        final List<UserExtendVO> list = Optional.ofNullable(this.userClient.listExtendByIds(ids))
            .map(ResultVO::getBizData).orElse(null);
        if (ObjectUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final UserExtendVO currentUserInfo = list.stream().filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.getId(), userId)).findFirst().orElse(null);
        String avatar = currentUserInfo.getAvatar();
        String nickName = currentUserInfo.getNickName();
        entity.setSeq(this.getNextSeq(orgId, userId));
        entity.setMsgStatus(MsgStatusEnum.UN_READ);
        entity.setSenderId(userId);
        entity.setSenderAvatar(avatar);
        entity.setSenderNickName(nickName);

        final UserExtendVO receiverUserInfo = list.stream().filter(Objects::nonNull)
            .filter(m -> Objects.equals(m.getId(), receiverId)).findFirst().orElse(null);
        entity.setReceiverAvatar(receiverUserInfo.getAvatar());
        entity.setReceiverNickName(receiverUserInfo.getNickName());
        entity.setOrgId(orgId);
        final DirectMsgDO model = BeanUtil.copyProperties(entity, DirectMsgDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(DirectMsgDTO entity) {
        DirectMsgDO data = super.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final DirectMsgDO model = BeanUtil.copyProperties(entity, DirectMsgDO.class);
        return super.updateById(model);
    }

    @Override
    public List<DirectMsgVO> listByUnSendSuccess(Long userId, Long orgId) {
        final EsLambdaQueryWrapper<DirectMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(DirectMsgDO::getOrgId, orgId)
            .eq(DirectMsgDO::getReceiverId, userId)
            .ne(DirectMsgDO::getSendStatus, SendStatusEnum.SUCCESS);
        final List<DirectMsgDO> list = super.list(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, DirectMsgVO.class);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        List<DirectMsgDO> list = super.listById(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @Override
    public boolean editMsgStatus(String id, MsgStatusEnum msgStatus) {
        DirectMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        DirectMsgDO entity = DirectMsgDO.builder()
            .id(id)
            .msgStatus(msgStatus)
            .build();
        if (MsgStatusEnum.READ.equals(msgStatus)) {
            entity.setReadTime(LocalDateTime.now());
        }
        super.updateById(entity);
        return Boolean.TRUE;
    }

    @Override
    public IPage<DirectMsgPageVO> page(PageDTO<DirectMsgDO> page, DirectMsgSearchDTO entity) {
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getMsgStatus())) {
            entity.setMsgStatus(MsgStatusEnum.UN_READ);
        }
        final EsLambdaQueryWrapper<DirectMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(DirectMsgDO::getOrgId, orgId)
            .eq(DirectMsgDO::getMsgStatus, entity.getMsgStatus())
            .eq(DirectMsgDO::getSendStatus, entity.getSendStatus());

        if (Objects.nonNull(entity.getReceiverId())) {
            wrapper.eq(DirectMsgDO::getReceiverId, entity.getReceiverId());
        }
        if (Objects.nonNull(entity.getSenderId())) {
            wrapper.eq(DirectMsgDO::getSenderId, entity.getSenderId());
        }

        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<DirectMsgDO> modelPage = super.page(page, wrapper);
        final IPage<DirectMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, DirectMsgPageVO.class));
        return result;
    }

    @Override
    public DirectMsgVO getDetail(String id, String ossTplCode) {
        DirectMsgDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getSenderAvatar())) {
            relativePaths.add(data.getSenderAvatar());
        }
        if (CharSequenceUtil.isNotBlank(data.getReceiverAvatar())) {
            relativePaths.add(data.getReceiverAvatar());
        }
        List<FilePathVO> filePaths = DirectMsgServiceImpl.getFilePaths(ossTplCode, relativePaths, this.ossClient);
        final DirectMsgVO result = BeanUtil.copyProperties(data, DirectMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId    组织主键
     * @param senderId 发送人主键
     * @return Integer
     */
    private Integer getNextSeq(Long orgId, Long senderId) {
        EsLambdaQueryWrapper<DirectMsgDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper
            .eq(DirectMsgDO::getOrgId, orgId)
            .eq(DirectMsgDO::getSenderId, senderId)
            .max(DirectMsgDO::getSeq)
            .limit(0);

        SearchHits<DirectMsgDO> result =
            getOperations().search(
                wrapper.build(),
                DirectMsgDO.class
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

    /**
     * 通过相对路径获取.
     *
     * @param ossTplCode    对象存储模板配置编码
     * @param relativePaths 相对路径集合
     * @param ossClient     对象存储客户端
     * @return List<FilePathVO>
     */
    public static List<FilePathVO> getFilePaths(String ossTplCode, List<String> relativePaths, OssClient ossClient) {
        if (CharSequenceUtil.isNotBlank(ossTplCode) && CollUtil.isNotEmpty(relativePaths)) {
            try {
                return Optional.ofNullable(ossClient.listSignUrl(ossTplCode, relativePaths, null))
                    .map(ResultVO::getBizData).orElse(null);
            } catch (Exception ex) {
                log.warn(ExceptionConstant.EXCEPTION, ex);
            }
        }
        return null;
    }

    private void buildUserInfo(DirectMsgVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
            }
            if (CharSequenceUtil.isNotBlank(data.getReceiverAvatar()) && data.getReceiverAvatar().equals(p.getRelativePath())) {
                data.setReceiverAvatar(p.getAbsolutePath());
            }
        });
    }
}
