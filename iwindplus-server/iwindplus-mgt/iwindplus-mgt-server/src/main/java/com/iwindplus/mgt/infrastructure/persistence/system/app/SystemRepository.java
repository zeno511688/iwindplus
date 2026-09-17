/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.system.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.system.app.vo.SystemPageVO;
import com.iwindplus.mgt.application.service.system.app.dto.SystemSearchDTO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 系统聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class SystemRepository extends JoinCrudRepository<SystemMapper, SystemDO> {

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<SystemPageVO> page(SystemSearchDTO entity) {
        PageDTO<SystemDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .orderByDesc(SystemDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(SystemDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(SystemDO::getName, entity.getName().trim());
        }
        queryWrapper.select(SystemDO::getId, SystemDO::getCreatedTimestamp, SystemDO::getCreatedBy,
            SystemDO::getModifiedTimestamp, SystemDO::getModifiedBy,
            SystemDO::getVersion, SystemDO::getStatus, SystemDO::getName, SystemDO::getHideFlag, SystemDO::getBuildInFlag
        );
        final PageDTO<SystemDO> modelPage = super.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, SystemPageVO.class));
    }

    /**
     * 获取名称是否已存在.
     *
     * @param name 名称
     */
    public void getNameIsExist(String name) {
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .eq(SystemDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取编码是否已存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        final LambdaQueryWrapper<SystemDO> queryWrapper = Wrappers.lambdaQuery(SystemDO.class)
            .eq(SystemDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取下一个排序序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<SystemDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

}
