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
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.im.application.query.dto.SysNoticeMsgSearchDTO;
import com.iwindplus.im.application.query.vo.SysNoticeMsgPageVO;
import com.iwindplus.im.application.query.vo.SysNoticeMsgVO;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.configuration.ImProperty;
import com.iwindplus.im.infrastructure.persistence.es.SysNoticeMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.SysNoticeMsgRepository;
import com.iwindplus.integr.client.OssClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统通知消息查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeMsgQueryService {

    private final SysNoticeMsgRepository sysNoticeMsgRepository;
    private final OssClient ossClient;
    private final ImProperty property;

    /**
     * 列表.
     *
     * @param page   分页对象
     * @param entity 对象
     * @return IPage<SysNoticeMsgPageVO>
     */
    public IPage<SysNoticeMsgPageVO> page(PageDTO<SysNoticeMsgDO> page, SysNoticeMsgSearchDTO entity) {
        Long userId = entity.getCurrentUserId();
        Long orgId = entity.getOrgId();
        if (Objects.isNull(entity.getSenderId())) {
            entity.setSenderId(userId);
        }
        if (Objects.isNull(entity.getSendStatus())) {
            entity.setSendStatus(SendStatusEnum.SUCCESS);
        }
        final EsLambdaQueryWrapper<SysNoticeMsgDO> wrapper = EsWrappers.<SysNoticeMsgDO>lambdaQuery()
            .eq(SysNoticeMsgDO::getOrgId, orgId)
            .eq(SysNoticeMsgDO::getSenderId, entity.getSenderId())
            .eq(SysNoticeMsgDO::getSendStatus, entity.getSendStatus());
        if (CharSequenceUtil.isNotBlank(entity.getTitle())) {
            wrapper.like(SysNoticeMsgDO::getTitle, entity.getTitle());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<SysNoticeMsgDO> modelPage = this.sysNoticeMsgRepository.page(page, wrapper);
        final IPage<SysNoticeMsgPageVO> result = modelPage.convert(model -> BeanUtil.copyProperties(model, SysNoticeMsgPageVO.class));
        return result;
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return SysNoticeMsgVO
     */
    public SysNoticeMsgVO getDetail(String id) {
        SysNoticeMsgDO data = this.sysNoticeMsgRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        List<String> relativePaths = Lists.newArrayList();
        if (CharSequenceUtil.isNotBlank(data.getSenderAvatar())) {
            relativePaths.add(data.getSenderAvatar());
        }
        List<FilePathVO> filePaths = this.getFilePaths(
            property.getOss().getCode(), property.getOss().getTplCode(), relativePaths, this.ossClient);
        final SysNoticeMsgVO result = BeanUtil.copyProperties(data, SysNoticeMsgVO.class);
        this.buildUserInfo(result, filePaths);
        return result;
    }

    private void buildUserInfo(SysNoticeMsgVO data, List<FilePathVO> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return;
        }
        filePaths.forEach(p -> {
            if (CharSequenceUtil.isNotBlank(data.getSenderAvatar()) && data.getSenderAvatar().equals(p.getRelativePath())) {
                data.setSenderAvatar(p.getAbsolutePath());
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
