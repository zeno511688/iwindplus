/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.server.dal.mapper.system.IpBlackListMapper;
import com.iwindplus.mgt.server.dal.model.system.IpBlackListDO;
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
