/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.upms.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseExtendVO;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.ResourcePageVO;
import com.iwindplus.mgt.application.service.upms.permission.dto.ResourceSearchDTO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ResourcePageVO> page(ResourceSearchDTO entity) {
        PageDTO<ResourceDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ResourceDO> queryWrapper = Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getMenuId, entity.getMenuId())
            .orderByDesc(ResourceDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ResourceDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(ResourceDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.like(ResourceDO::getName, entity.getName().trim());
        }
        queryWrapper.select(ResourceDO::getId, ResourceDO::getCreatedTimestamp, ResourceDO::getCreatedBy,
            ResourceDO::getModifiedTimestamp, ResourceDO::getModifiedBy, ResourceDO::getVersion, ResourceDO::getStatus,
            ResourceDO::getCode, ResourceDO::getName, ResourceDO::getBuildInFlag,
            ResourceDO::getApiUrls, ResourceDO::getSeq, ResourceDO::getMenuId
        );
        final PageDTO<ResourceDO> modelPage = super.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ResourcePageVO.class));
    }

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
     * 查询所有.
     *
     * @return List<ResourceDO>
     */
    public List<ResourceDO> listAll() {
        return super.getBaseMapper().selectList(Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getStatus, EnableStatusEnum.ENABLE)
            .orderByAsc(List.of(ResourceDO::getSeq)));
    }

    /**
     * 用户资源权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<ResourceBaseVO>
     */
    public List<ResourceBaseVO> listResourceCheckedByUserId(Long orgId, Long userId) {
        final List<ResourceBaseExtendVO> list = super.getBaseMapper().selectListCheckedByUserId(orgId, userId, null);;

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
