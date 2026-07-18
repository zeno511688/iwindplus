/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import com.iwindplus.mgt.domain.dto.power.RoleDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.vo.power.RoleBaseVO;
import com.iwindplus.mgt.domain.vo.power.RoleVO;
import com.iwindplus.mgt.server.dal.mapper.power.RoleMapper;
import com.iwindplus.mgt.server.dal.model.power.RoleDO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class RoleRepository extends JoinCrudRepository<RoleMapper, RoleDO> {

    /**
     * 批量初始化.
     *
     * @param entities 对象集合
     * @return List<RoleVO>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RoleVO> saveBatchInit(List<RoleDTO> entities) {
        List<RoleVO> resultList = new ArrayList<>(10);
        AtomicReference<Integer> seq = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean(true);
        List<RoleDTO> saveList = new ArrayList<>(10);
        List<RoleDTO> existList = new ArrayList<>(10);
        entities.forEach(entity -> {
            final RoleDO data = super.getOne(Wrappers.lambdaQuery(RoleDO.class)
                .eq(RoleDO::getName, entity.getName())
                .eq(RoleDO::getOrgId, entity.getOrgId()));
            if (Objects.isNull(data)) {
                if (flag.get()) {
                    seq.set(this.getNextSeq(entity.getOrgId()));
                    flag.set(false);
                }
                entity.setSeq(seq.getAndSet(seq.get() + 1));
                saveList.add(entity);
            } else {
                final RoleDTO dto = BeanUtil.copyProperties(data, RoleDTO.class);
                existList.add(dto);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            final List<RoleDO> doList = BeanUtil.copyToList(saveList, RoleDO.class);
            super.saveBatch(doList, Constants.DEFAULT_BATCH_SIZE);
            final List<RoleVO> voList = BeanUtil.copyToList(doList, RoleVO.class);
            resultList.addAll(voList);
        }
        if (CollUtil.isNotEmpty(existList)) {
            final List<RoleVO> voList = BeanUtil.copyToList(existList, RoleVO.class);
            resultList.addAll(voList);
        }
        return resultList;
    }

    /**
     * 初始化角色至（客户端，组织），返回新旧id map.
     *
     * @param orgId   组织主键
     * @param roleIds 角色主键集合
     * @return Map<Long, Long>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Long> editInitToClient(Long orgId, Set<Long> roleIds) {
        Map<Long, Long> idMap = new HashMap<>(16);
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }
        final List<RoleDO> entities = super.list(Wrappers.lambdaQuery(RoleDO.class)
            .in(RoleDO::getId, roleIds).orderByAsc(RoleDO::getSeq));
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyMap();
        }
        final List<RoleDO> paramList = BeanUtil.copyToList(entities, RoleDO.class);
        this.buildInitList(orgId, paramList);
        final List<RoleDTO> dtoList = BeanUtil.copyToList(paramList, RoleDTO.class);
        final List<RoleVO> newList = this.saveBatchInit(dtoList);
        final List<RoleDO> doList = BeanUtil.copyToList(newList, RoleDO.class);
        this.buildIdMap(idMap, entities, doList);
        return idMap;
    }

    /**
     * 获取名称是否已存在.
     *
     * @param name  名称
     * @param orgId 组织ID
     */
    public void getNameIsExist(String name, Long orgId) {
        final LambdaQueryWrapper<RoleDO> queryWrapper = Wrappers.lambdaQuery(RoleDO.class)
            .eq(RoleDO::getName, name).eq(RoleDO::getOrgId, orgId);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取编码是否已存在.
     *
     * @param code  编码
     * @param orgId 组织ID
     */
    public void getCodeIsExist(String code, Long orgId) {
        final LambdaQueryWrapper<RoleDO> queryWrapper = Wrappers.lambdaQuery(RoleDO.class)
            .eq(RoleDO::getCode, code).eq(RoleDO::getOrgId, orgId);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (result) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 获取下一个排序序号.
     *
     * @param orgId 组织ID
     * @return Integer
     */
    public Integer getNextSeq(Long orgId) {
        QueryWrapper<RoleDO> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(orgId)) {
            queryWrapper.lambda().eq(RoleDO::getOrgId, orgId);
        }
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 查询用户角色权限.
     *
     * @param orgId  组织主键
     * @param userId 用户主键
     * @return List<RoleBaseVO>
     */
    public List<RoleBaseVO> listCheckedByUserId(Long orgId, Long userId) {
        List<RoleBaseVO> list = super.getBaseMapper().selectListCheckedByUserId(orgId, userId);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.stream()
            .map(m -> RoleBaseVO.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .build())
            .sorted(Comparator.comparing(RoleBaseVO::getName))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 获取默认角色.
     *
     * @param orgId 组织主键
     * @return Set<Long>
     */
    public Set<Long> listDefaultRoles(Long orgId) {
        final List<RoleDO> list = super.list(Wrappers.lambdaQuery(RoleDO.class)
            .eq(RoleDO::getDefaultFlag, Boolean.TRUE)
            .eq(RoleDO::getOrgId, orgId)
            .select(RoleDO::getId));
        if (CollUtil.isNotEmpty(list)) {
            return list.stream().map(RoleDO::getId).collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    /**
     * 取消默认角色.
     *
     * @param roleId 角色主键
     * @param orgId  组织主键
     * @return List<RoleDO>
     */
    public List<RoleDO> editCancelDefault(Long roleId, Long orgId) {
        List<RoleDO> defaultList = super.list(Wrappers.lambdaQuery(RoleDO.class)
            .eq(RoleDO::getDefaultFlag, Boolean.TRUE).eq(RoleDO::getOrgId, orgId)
            .select(RoleDO::getId, RoleDO::getVersion));
        if (CollUtil.isEmpty(defaultList)) {
            return Collections.emptyList();
        }
        if (Objects.nonNull(roleId)) {
            defaultList.removeIf(m -> roleId.equals(m.getId()));
        }
        if (CollUtil.isEmpty(defaultList)) {
            return Collections.emptyList();
        }

        return defaultList.stream().map(m -> {
            RoleDO param = new RoleDO();
            param.setId(m.getId());
            param.setDefaultFlag(Boolean.FALSE);
            param.setVersion(m.getVersion());
            return param;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private void buildInitList(Long orgId, List<RoleDO> entities) {
        entities.forEach(entity -> {
            entity.setId(null);
            entity.setVersion(0);
            entity.setOrgId(orgId);
            entity.setRemark(MgtConstant.REMARK_INIT);
        });
    }

    private void buildIdMap(Map<Long, Long> idMap, List<RoleDO> oldList, List<RoleDO> newList) {
        oldList.forEach(oldData -> newList.forEach(newData -> {
            if (oldData.getName().equals(newData.getName())) {
                idMap.put(oldData.getId(), newData.getId());
            }
        }));
    }

}
