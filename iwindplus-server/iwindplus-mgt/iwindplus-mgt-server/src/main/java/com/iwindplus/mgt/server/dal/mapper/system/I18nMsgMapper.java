/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.system;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.system.I18nMsgQueryDTO;
import com.iwindplus.mgt.domain.dto.system.I18nMsgSearchDTO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgExtendVO;
import com.iwindplus.mgt.domain.vo.system.I18nMsgPageVO;
import com.iwindplus.mgt.server.dal.model.system.I18nMsgDO;
import com.iwindplus.mgt.server.dal.model.system.I18nProjectDO;
import java.util.List;
import java.util.Objects;
import org.apache.ibatis.annotations.Mapper;

/**
 * 国际化消息数据访问层接口类.
 *
 * @author zengdegui
 * @since 2025/07/14 22:11
 */
@Mapper
public interface I18nMsgMapper extends MPJBaseMapper<I18nMsgDO> {

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return IPage<I18nMsgPageVO>
     */
    default IPage<I18nMsgPageVO> selectPageByCondition(I18nMsgSearchDTO entity) {
        PageDTO<I18nMsgPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        MPJLambdaWrapper<I18nMsgDO> queryWrapper = new MPJLambdaWrapper<>(I18nMsgDO.class)
            .selectAll(I18nMsgDO.class)
            .leftJoin(I18nProjectDO.class, I18nProjectDO::getId, I18nMsgDO::getProjectId)
            .selectAs(I18nProjectDO::getStatus, I18nMsgPageVO::getProjectStatus)
            .selectAs(I18nProjectDO::getPlatformType, I18nMsgPageVO::getProjectPlatformType)
            .selectAs(I18nProjectDO::getCode, I18nMsgPageVO::getProjectCode)
            .selectAs(I18nProjectDO::getName, I18nMsgPageVO::getProjectName)
            .selectAs(I18nProjectDO::getFileName, I18nMsgPageVO::getProjectFileName)
            .orderByDesc(I18nMsgDO::getModifiedTime)
            .eq(I18nProjectDO::getStatus, EnableStatusEnum.ENABLE);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(I18nMsgDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(I18nMsgDO::getCode, entity.getCode().trim());
        }
        if (Objects.nonNull(entity.getProjectId())) {
            queryWrapper.eq(I18nProjectDO::getId, entity.getProjectId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getProjectCode())) {
            queryWrapper.eq(I18nProjectDO::getCode, entity.getProjectCode().trim());
        }
        return this.selectJoinPage(page, I18nMsgPageVO.class, queryWrapper);
    }

    /**
     * 表连接条件查询获取国际化消息.
     *
     * @param entity 对象
     * @return List<I18nMsgExtendVO>
     */
    default List<I18nMsgExtendVO> selectListByCondition(I18nMsgQueryDTO entity) {
        MPJLambdaWrapper<I18nMsgDO> queryWrapper = new MPJLambdaWrapper<>(I18nMsgDO.class)
            .selectAll(I18nMsgDO.class)
            .leftJoin(I18nProjectDO.class, I18nProjectDO::getId, I18nMsgDO::getProjectId)
            .selectAs(I18nProjectDO::getStatus, I18nMsgExtendVO::getProjectStatus)
            .selectAs(I18nProjectDO::getPlatformType, I18nMsgExtendVO::getProjectPlatformType)
            .selectAs(I18nProjectDO::getCode, I18nMsgExtendVO::getProjectCode)
            .selectAs(I18nProjectDO::getName, I18nMsgExtendVO::getProjectName)
            .selectAs(I18nProjectDO::getFileName, I18nMsgExtendVO::getProjectFileName);
        if (Objects.nonNull(entity.getId())) {
            queryWrapper.eq(I18nMsgDO::getId, entity.getId());
        }
        if (Objects.nonNull(entity.getMsgStatus())) {
            queryWrapper.eq(I18nMsgDO::getStatus, entity.getMsgStatus());
        }
        if (Objects.nonNull(entity.getProjectId())) {
            queryWrapper.eq(I18nProjectDO::getId, entity.getProjectId());
        }
        if (Objects.nonNull(entity.getProjectStatus())) {
            queryWrapper.eq(I18nProjectDO::getStatus, entity.getProjectStatus());
        }
        if (Objects.nonNull(entity.getProjectPlatformType())) {
            queryWrapper.eq(I18nProjectDO::getPlatformType, entity.getProjectPlatformType());
        }
        return this.selectJoinList(I18nMsgExtendVO.class, queryWrapper);
    }
}
