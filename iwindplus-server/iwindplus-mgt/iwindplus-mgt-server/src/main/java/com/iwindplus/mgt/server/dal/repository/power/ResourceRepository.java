/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.ResourceTypeEnum;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import com.iwindplus.mgt.server.dal.mapper.power.ResourceMapper;
import com.iwindplus.mgt.server.dal.model.power.ResourceDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 资源聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class ResourceRepository extends JoinCrudRepository<ResourceMapper, ResourceDO> {

    /**
     * 获取名称是否存在.
     *
     * @param name   名称
     * @param menuId 菜单主键
     */
    public void getNameIsExist(String name, Long menuId) {
        final LambdaQueryWrapper<ResourceDO> queryWrapper = Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getMenuId, menuId)
            .eq(ResourceDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取API路径是否存在.
     *
     * @param apiUrl API路径
     * @param menuId 菜单主键
     */
    public void getApiUrlIsExist(String apiUrl, Long menuId) {
        final LambdaQueryWrapper<ResourceDO> queryWrapper = Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getMenuId, menuId)
            .eq(ResourceDO::getApiUrl, apiUrl);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.API_URL_EXIST);
        }
    }

    /**
     * 获取编码是否存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        final LambdaQueryWrapper<ResourceDO> queryWrapper = Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取下一个排序序号.
     *
     * @param menuId 菜单主键
     * @return Integer
     */
    public Integer getNextSeq(Long menuId) {
        QueryWrapper<ResourceDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ResourceDO::getMenuId, menuId);
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 用户权限.
     *
     * @param orgId         组织主键
     * @param userId        用户主键
     * @param types         资源类型集合
     * @param requestMethod 请求方式
     * @param apiUrl        API路径
     * @return List<ResourceBaseExtendVO>
     */
    public List<ResourceBaseExtendVO> listCheckedByUserId(Long orgId, Long userId, List<ResourceTypeEnum> types
        , String requestMethod, String apiUrl) {
        return super.getBaseMapper().selectListCheckedByUserId(orgId, userId, types, requestMethod, apiUrl);
    }

    /**
     * 查询所有.
     *
     * @return List<ResourceDO>
     */
    public List<ResourceDO> listAll() {
        return super.getBaseMapper().selectList(Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getStatus, EnableStatusEnum.ENABLE)
            .orderByAsc(Arrays.asList(ResourceDO::getApiUrl, ResourceDO::getSeq)));
    }

    /**
     * 用户按钮权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<ResourceBaseVO>
     */
    public List<ResourceBaseVO> listButtonCheckedByUserId(Long orgId, Long userId) {
        final List<ResourceTypeEnum> types = Arrays.asList(ResourceTypeEnum.BUTTON);
        final List<ResourceBaseExtendVO> list = this.listCheckedByUserId(orgId, userId, types, null, null);

        return this.buildResourceBaseVO(list);
    }

    private List<ResourceBaseVO> buildResourceBaseVO(List<ResourceBaseExtendVO> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList())
            .stream()
            .map(m -> ResourceBaseVO.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .build())
            .sorted(Comparator.comparing(ResourceBaseVO::getName))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
