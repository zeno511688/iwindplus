/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.system.ApiWhiteListMapper;
import com.iwindplus.mgt.server.dal.model.system.ApiWhiteListDO;
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
     * 检查编码是否存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        final LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .eq(ApiWhiteListDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
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
