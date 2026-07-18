/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.PlatformTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.dto.system.I18nProjectExtendDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.system.I18nProjectMapper;
import com.iwindplus.mgt.server.dal.model.system.I18nMsgDO;
import com.iwindplus.mgt.server.dal.model.system.I18nProjectDO;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 国际化项目聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
@RequiredArgsConstructor
public class I18nProjectRepository extends JoinCrudRepository<I18nProjectMapper, I18nProjectDO> {

    private final I18nMsgRepository i18nMsgRepository;

    /**
     * 保存/更新国际化项目.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public I18nProjectDO saveOrUpdateI18nProject(I18nProjectExtendDTO entity) {
        final I18nProjectDO model = BeanUtil.copyProperties(entity, I18nProjectDO.class);
        super.saveOrUpdate(model);
        List<I18nMsgDO> i18nMsgList = this.i18nMsgRepository.buildI18nMsgList(entity.getContent(), model.getId());
        if (CollUtil.isNotEmpty(i18nMsgList)) {
            this.i18nMsgRepository.saveOrUpdateBatch(i18nMsgList, Constants.DEFAULT_BATCH_SIZE);
        }
        return model;
    }

    /**
     * 批量删除（项目主键集合）.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByProjectIds(List<Long> ids) {
        final boolean data = super.removeByIds(ids);
        this.i18nMsgRepository.removeByIds(ids);
        return data;
    }

    /**
     * 获取编码是否已存在.
     *
     * @param platformType 平台类型
     * @param code         编码
     */
    public void getCodeIsExist(PlatformTypeEnum platformType, String code) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(I18nProjectDO.class)
            .eq(I18nProjectDO::getPlatformType, platformType)
            .eq(I18nProjectDO::getCode, code)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 检查名称是否存在.
     *
     * @param platformType 平台类型
     * @param name         名称
     */
    public void getNameIsExist(PlatformTypeEnum platformType, String name) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(I18nProjectDO.class)
            .eq(I18nProjectDO::getPlatformType, platformType)
            .eq(I18nProjectDO::getName, name)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查文件名称是否存在.
     *
     * @param platformType 平台类型
     * @param fileName     文件名称
     */
    public void getFileNameIsExist(PlatformTypeEnum platformType, String fileName) {
        boolean result = SqlHelper.retBool(super.count(Wrappers.lambdaQuery(I18nProjectDO.class)
            .eq(I18nProjectDO::getPlatformType, platformType)
            .eq(I18nProjectDO::getFileName, fileName)));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(BizCodeEnum.FILE_NAME_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @param platformType 平台类型
     * @return Integer
     */
    public Integer getNextSeq(PlatformTypeEnum platformType) {
        QueryWrapper<I18nProjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(I18nProjectDO::getPlatformType, platformType);
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}
