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
import com.iwindplus.mgt.server.dal.mapper.system.ServerApiMapper;
import com.iwindplus.mgt.server.dal.model.system.ServerApiDO;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;

/**
 * 服务API聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ServerApiRepository extends JoinCrudRepository<ServerApiMapper, ServerApiDO>  {

    /**
     * 获取控制器名称是否存在
     *
     * @param appName        应用名称
     * @param controllerName 控制器名称
     */
    public void getControllerNameIsExist(String appName, String controllerName) {
        LambdaQueryWrapper<ServerApiDO> queryWrapper = Wrappers.lambdaQuery(ServerApiDO.class)
            .eq(ServerApiDO::getAppName, appName)
            .eq(ServerApiDO::getControllerName, controllerName);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CONTROLLER_NAME_EXIST);
        }
    }

    /**
     * 获取API名称是否存在
     *
     * @param appName 应用名称
     * @param apiName API名称
     */
    public void getApiNameIsExist(String appName, String apiName) {
        LambdaQueryWrapper<ServerApiDO> queryWrapper = Wrappers.lambdaQuery(ServerApiDO.class)
            .eq(ServerApiDO::getAppName, appName)
            .eq(ServerApiDO::getApiName, apiName);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.API_NAME_EXIST);
        }
    }

    /**
     * 获取API路径是否存在.
     *
     * @param appName 应用名称
     * @param apiUrl  API路径
     */
    public void getApiUrlIsExist(String appName, String apiUrl) {
        LambdaQueryWrapper<ServerApiDO> queryWrapper = Wrappers.lambdaQuery(ServerApiDO.class)
            .eq(ServerApiDO::getAppName, appName)
            .eq(ServerApiDO::getApiUrl, apiUrl);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.API_URL_EXIST);
        }
    }

    /**
     * 获取下一个排序序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<ServerApiDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

}
