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
import com.iwindplus.mgt.domain.dto.power.DepartmentDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.vo.power.DepartmentVO;
import com.iwindplus.mgt.server.dal.mapper.power.DepartmentMapper;
import com.iwindplus.mgt.server.dal.model.power.DepartmentDO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class DepartmentRepository extends JoinCrudRepository<DepartmentMapper, DepartmentDO> {

    /**
     * 批量初始化.
     *
     * @param entities 对象集合
     * @return List<DepartmentVO>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<DepartmentVO> saveBatchInit(List<DepartmentDTO> entities) {
        List<DepartmentVO> resultList = new ArrayList<>(10);
        AtomicReference<Integer> seq = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean(true);
        List<DepartmentDTO> saveList = new ArrayList<>(10);
        List<DepartmentDTO> existList = new ArrayList<>(10);
        entities.forEach(entity -> {
            final LambdaQueryWrapper<DepartmentDO> queryWrapper = Wrappers.lambdaQuery(DepartmentDO.class)
                .eq(DepartmentDO::getName, entity.getName())
                .eq(DepartmentDO::getOrgId, entity.getOrgId());
            if (Objects.nonNull(entity.getParentId())) {
                queryWrapper.eq(DepartmentDO::getParentId, entity.getParentId());
            }
            final DepartmentDO data = super.getOne(queryWrapper);
            if (Objects.isNull(data)) {
                if (flag.get()) {
                    seq.set(this.getNextSeq(entity.getOrgId(), entity.getParentId()));
                    flag.set(false);
                }
                entity.setSeq(seq.getAndSet(seq.get() + 1));
                saveList.add(entity);
            } else {
                final DepartmentDTO dto = BeanUtil.copyProperties(data, DepartmentDTO.class);
                existList.add(dto);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            final List<DepartmentDO> doList = BeanUtil.copyToList(saveList, DepartmentDO.class);
            super.saveBatch(doList, Constants.DEFAULT_BATCH_SIZE);
            final List<DepartmentVO> voList = BeanUtil.copyToList(doList, DepartmentVO.class);
            resultList.addAll(voList);
        }
        if (CollUtil.isNotEmpty(existList)) {
            final List<DepartmentVO> voList = BeanUtil.copyToList(existList, DepartmentVO.class);
            resultList.addAll(voList);
        }
        return resultList;
    }

    /**
     * 初始化至组织.
     *
     * @param orgId         组织主键
     * @param departmentIds 部门主键集合
     * @return Map<Long, Long>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Long> editInitToOrg(Long orgId, Set<Long> departmentIds) {
        if (Objects.isNull(orgId)) {
            return Collections.emptyMap();
        }
        Map<Long, Long> idMap = new HashMap<>(16);
        if (CollUtil.isEmpty(departmentIds)) {
            return Collections.emptyMap();
        }
        final List<DepartmentDO> entities = super.list(Wrappers.lambdaQuery(DepartmentDO.class)
            .in(DepartmentDO::getId, departmentIds).orderByAsc(DepartmentDO::getSeq));
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyMap();
        }
        // 按级别分组排序，级别为1为最顶级
        final Map<Integer, List<DepartmentDO>> groupLevelMap = entities.stream().sorted(Comparator.comparing(DepartmentDO::getSeq))
            .collect(Collectors.groupingBy(DepartmentDO::getLevel, TreeMap::new, Collectors.toCollection(ArrayList::new)));
        for (Map.Entry<Integer, List<DepartmentDO>> groupLevelMapEntry : groupLevelMap.entrySet()) {
            Integer groupLevelKey = groupLevelMapEntry.getKey();
            final List<DepartmentDO> groupLevelMapList = groupLevelMapEntry.getValue();
            if (groupLevelKey == 1) {
                final List<DepartmentDO> paramList = BeanUtil.copyToList(groupLevelMapList, DepartmentDO.class);
                this.buildInitList(orgId, null, paramList);
                final List<DepartmentDTO> dtoList = BeanUtil.copyToList(paramList, DepartmentDTO.class);
                List<DepartmentVO> parentNewtList = this.saveBatchInit(dtoList);
                final List<DepartmentDO> doList = BeanUtil.copyToList(parentNewtList, DepartmentDO.class);
                this.buildIdMap(idMap, groupLevelMapList, doList);
            } else {
                final Map<Long, List<DepartmentDO>> groupParentIdMap = groupLevelMapList.stream().sorted(Comparator.comparing(DepartmentDO::getSeq))
                    .collect(Collectors.groupingBy(DepartmentDO::getParentId, LinkedHashMap::new, Collectors.toList()));
                for (Map.Entry<Long, List<DepartmentDO>> groupParentIdMapEntry : groupParentIdMap.entrySet()) {
                    final Long parentId = groupParentIdMapEntry.getKey();
                    final List<DepartmentDO> groupParentIdMapList = groupParentIdMapEntry.getValue();
                    final Long newId = idMap.get(parentId);
                    final List<DepartmentDO> paramList = BeanUtil.copyToList(groupParentIdMapList, DepartmentDO.class);
                    this.buildInitList(orgId, newId, paramList);
                    final List<DepartmentDTO> dtoList = BeanUtil.copyToList(paramList, DepartmentDTO.class);
                    final List<DepartmentVO> childrenNewList = this.saveBatchInit(dtoList);
                    final List<DepartmentDO> doList = BeanUtil.copyToList(childrenNewList, DepartmentDO.class);
                    this.buildIdMap(idMap, groupParentIdMapList, doList);
                }
            }
        }
        return idMap;
    }

    /**
     * 检查名称是否已存在.
     *
     * @param name     名称
     * @param orgId    组织主键
     * @param parentId 父类主键
     */
    public void getNameIsExist(String name, Long orgId, Long parentId) {
        final LambdaQueryWrapper<DepartmentDO> queryWrapper = Wrappers.lambdaQuery(DepartmentDO.class)
            .eq(DepartmentDO::getOrgId, orgId)
            .eq(DepartmentDO::getName, name);
        if (Objects.nonNull(parentId)) {
            queryWrapper.eq(DepartmentDO::getParentId, parentId);
        }
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查编码是否已存在.
     *
     * @param code     编码
     * @param orgId    组织主键
     * @param parentId 父类主键
     */
    public void getCodeIsExist(String code, Long orgId, Long parentId) {
        final LambdaQueryWrapper<DepartmentDO> queryWrapper = Wrappers.lambdaQuery(DepartmentDO.class)
            .eq(DepartmentDO::getOrgId, orgId)
            .eq(DepartmentDO::getCode, code);
        if (Objects.nonNull(parentId)) {
            queryWrapper.eq(DepartmentDO::getParentId, parentId);
        }
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId    组织主键
     * @param parentId 父类主键
     * @return Integer
     */
    public Integer getNextSeq(Long orgId, Long parentId) {
        QueryWrapper<DepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DepartmentDO::getOrgId, orgId);
        if (Objects.nonNull(parentId)) {
            queryWrapper.lambda().eq(DepartmentDO::getParentId, parentId);
        }
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 查询级别.
     *
     * @param orgId    组织主键
     * @param parentId 父类主键
     * @return Integer
     */
    public Integer getLevel(Long orgId, Long parentId) {
        if (Objects.isNull(parentId)) {
            return 1;
        }
        QueryWrapper<DepartmentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DepartmentDO::getOrgId, orgId);
        queryWrapper.lambda().eq(DepartmentDO::getId, parentId);
        queryWrapper.lambda().select(DepartmentDO::getLevel);
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    private void buildInitList(Long orgId, Long parentId, List<DepartmentDO> entities) {
        entities.forEach(entity -> {
            entity.setId(null);
            entity.setVersion(0);
            entity.setParentId(parentId);
            entity.setOrgId(orgId);
            entity.setRemark(MgtConstant.REMARK_INIT);
        });
    }

    private void buildIdMap(Map<Long, Long> idMap, List<DepartmentDO> oldList, List<DepartmentDO> newList) {
        oldList.forEach(oldData -> newList.forEach(newData -> {
            if (oldData.getName().equals(newData.getName())) {
                idMap.put(oldData.getId(), newData.getId());
            }
        }));
    }

}
