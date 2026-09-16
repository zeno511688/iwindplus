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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.im.application.query.dto.ChatGroupSearchDTO;
import com.iwindplus.im.application.query.vo.ChatGroupBaseVO;
import com.iwindplus.im.application.query.vo.ChatGroupPageVO;
import com.iwindplus.im.application.query.vo.ChatGroupVO;
import com.iwindplus.im.infrastructure.configuration.ImProperty;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupDO;
import com.iwindplus.im.infrastructure.persistence.mysql.ChatGroupRepository;
import com.iwindplus.integr.client.OssClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天群查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGroupQueryService {

    private final OssClient ossClient;
    private final ChatGroupRepository chatGroupRepository;
    private final ImProperty imProperty;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<ChatGroupPageVO>
     */
    public IPage<ChatGroupPageVO> page(PageDTO<ChatGroupDO> page, ChatGroupSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(DbConstant.MODIFIED_TIMESTAMP);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = "cg." + order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        final IPage<ChatGroupPageVO> modelPage = this.chatGroupRepository.getBaseMapper().selectPageByCondition(page, entity);
        return modelPage;
    }

    /**
     * 通过主键端查找.
     *
     * @param id         主键
     * @return ChatGroupVO
     */
    public ChatGroupVO getDetail(Long id) {
        ChatGroupDO data = this.chatGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getGroupAvatar())) {
            relativePaths.add(data.getGroupAvatar());
        }
        if (CharSequenceUtil.isNotBlank(data.getGroupQrcode())) {
            relativePaths.add(data.getGroupQrcode());
        }
        List<FilePathVO> filePaths = this.getFilePaths(
            imProperty.getOss().getCode(), imProperty.getOss().getTplCode(), relativePaths, this.ossClient);
        final ChatGroupVO result = BeanUtil.copyProperties(data, ChatGroupVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    /**
     * 根据用户主键查询.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return List<Long>
     */
    public List<Long> listByUserId(Long userId, Long orgId) {
        return this.chatGroupRepository.getBaseMapper().selectByUserId(userId, orgId);
    }

    /**
     * 通过组织主键查询.
     *
     * @param orgId 组织主键
     * @return List<ChatGroupBaseVO>
     */
    public List<ChatGroupBaseVO> listByOrgId(Long orgId) {
        final List<ChatGroupDO> list = this.chatGroupRepository.list(Wrappers.lambdaQuery(ChatGroupDO.class)
            .eq(ChatGroupDO::getOrgId, orgId).select(ChatGroupDO::getId, ChatGroupDO::getGroupName));
        if (CollUtil.isNotEmpty(list)) {
            return BeanUtil.copyToList(list, ChatGroupBaseVO.class);
        }
        return null;
    }

    private void buildUserInfo(ChatGroupVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getGroupAvatar()) && data.getGroupAvatar().equals(p.getRelativePath())) {
                data.setGroupAvatar(p.getAbsolutePath());
            }
            if (CharSequenceUtil.isNotBlank(data.getGroupQrcode()) && data.getGroupQrcode().equals(p.getRelativePath())) {
                data.setGroupQrcode(p.getAbsolutePath());
            }
        });
    }

    private List<FilePathVO> getFilePaths(String ossCode, String ossTplCode, List<String> relativePaths, OssClient ossClient) {
        if (CharSequenceUtil.isNotBlank(ossTplCode) && CollUtil.isNotEmpty(relativePaths)) {
            try {
                return Optional.ofNullable(ossClient.listSignUrl(ossCode, ossTplCode, relativePaths, null))
                    .map(ResultVO::getBizData).orElse(null);
            } catch (Exception ex) {
                log.warn(ExceptionConstant.EXCEPTION, ex);
            }
        }
        return null;
    }
}
