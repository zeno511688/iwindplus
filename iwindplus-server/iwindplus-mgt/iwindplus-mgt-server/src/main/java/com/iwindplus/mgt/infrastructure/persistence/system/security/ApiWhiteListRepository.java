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
import com.iwindplus.mgt.api.system.dto.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.api.system.vo.ApiWhiteListPageVO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * API白名单聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ApiWhiteListRepository extends JoinCrudRepository<ApiWhiteListMapper, ApiWhiteListDO> {

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ApiWhiteListPageVO> page(ApiWhiteListSearchDTO entity) {
        PageDTO<ApiWhiteListDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .orderByDesc(ApiWhiteListDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ApiWhiteListDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(ApiWhiteListDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl())) {
            queryWrapper.like(ApiWhiteListDO::getApiUrl, entity.getApiUrl().trim());
        }
        final PageDTO<ApiWhiteListDO> modelPage = super.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ApiWhiteListPageVO.class));
    }

    /**
     * 检查名称是否存在.
     *
     * @param name 名称
     */
    public void getNameIsExist(String name) {
        final LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .eq(ApiWhiteListDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查API路径是否存在.
     *
     * @param apiUrl API路径
     */
    public void getApiUrlIsExist(String apiUrl) {
        final LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .eq(ApiWhiteListDO::getApiUrl, apiUrl);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.URL_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<ApiWhiteListDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}
