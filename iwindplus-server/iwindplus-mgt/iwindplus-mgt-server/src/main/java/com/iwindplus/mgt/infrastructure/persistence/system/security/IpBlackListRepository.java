/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.system.security;

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
import com.iwindplus.mgt.api.system.dto.IpBlackListSearchDTO;
import com.iwindplus.mgt.api.system.vo.IpBlackListPageVO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * IP黑名单聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class IpBlackListRepository extends JoinCrudRepository<IpBlackListMapper, IpBlackListDO> {

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<IpBlackListPageVO> page(IpBlackListSearchDTO entity) {
        PageDTO<IpBlackListDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<IpBlackListDO> queryWrapper = Wrappers.lambdaQuery(IpBlackListDO.class)
            .orderByDesc(IpBlackListDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(IpBlackListDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            queryWrapper.like(IpBlackListDO::getIp, entity.getIp().trim());
        }
        final PageDTO<IpBlackListDO> modelPage = super.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, IpBlackListPageVO.class));
    }

    /**
     * 检查IP是否存在.
     *
     * @param ip IP
     */
    public void getIpIsExist(String ip) {
        QueryWrapper<IpBlackListDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IpBlackListDO::getIp, ip);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<IpBlackListDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}
