/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.google.common.collect.Maps;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant;
import com.iwindplus.mgt.domain.dto.power.UserGroupDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.vo.power.UserGroupVO;
import com.iwindplus.mgt.server.dal.mapper.power.UserGroupMapper;
import com.iwindplus.mgt.server.dal.model.power.UserGroupDO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户组聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserGroupRepository extends JoinCrudRepository<UserGroupMapper, UserGroupDO> {

    /**
     * 批量初始化.
     *
     * @param entities 对象集合
     * @return List<UserGroupVO>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<UserGroupVO> saveBatchInit(List<UserGroupDTO> entities) {
        List<UserGroupVO> resultList = new ArrayList<>(10);
        AtomicReference<Integer> seq = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean(true);
        List<UserGroupDTO> saveList = new ArrayList<>(10);
        List<UserGroupDTO> existList = new ArrayList<>(10);
        entities.forEach(entity -> {
            final UserGroupDO data = super.getOne(Wrappers.lambdaQuery(UserGroupDO.class)
                .eq(UserGroupDO::getName, entity.getName())
                .eq(UserGroupDO::getOrgId, entity.getOrgId()));
            if (Objects.isNull(data)) {
                if (flag.get()) {
                    seq.set(this.getNextSeq(entity.getOrgId()));
                    flag.set(false);
                }
                entity.setSeq(seq.getAndSet(seq.get() + 1));
                saveList.add(entity);
            } else {
                final UserGroupDTO dto = BeanUtil.copyProperties(data, UserGroupDTO.class);
                existList.add(dto);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            final List<UserGroupDO> doList = BeanUtil.copyToList(saveList, UserGroupDO.class);
            super.saveBatch(doList, DEFAULT_BATCH_SIZE);
            final List<UserGroupVO> voList = BeanUtil.copyToList(doList, UserGroupVO.class);
            resultList.addAll(voList);
        }
        if (CollUtil.isNotEmpty(existList)) {
            final List<UserGroupVO> voList = BeanUtil.copyToList(existList, UserGroupVO.class);
            resultList.addAll(voList);
        }
        return resultList;
    }

    /**
     * 初始化组至组织，返回新旧id map.
     *
     * @param orgId        组织主键（可选）
     * @param userGroupIds 用户组主键集合
     * @return Map<Long, Long>
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<Long, Long> editInitToOrg(Long orgId, Set<Long> userGroupIds) {
        if (Objects.isNull(orgId)) {
            return Collections.emptyMap();
        }
        Map<Long, Long> idMap = Maps.newHashMap();
        if (CollUtil.isEmpty(userGroupIds)) {
            return Collections.emptyMap();
        }
        final List<UserGroupDO> entities = super.list(Wrappers.lambdaQuery(UserGroupDO.class)
            .in(UserGroupDO::getId, userGroupIds).orderByAsc(UserGroupDO::getSeq));
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyMap();
        }
        final List<UserGroupDO> paramList = BeanUtil.copyToList(entities, UserGroupDO.class);
        this.buildInitList(orgId, paramList);
        final List<UserGroupDTO> dtoList = BeanUtil.copyToList(paramList, UserGroupDTO.class);
        final List<UserGroupVO> voList = this.saveBatchInit(dtoList);
        final List<UserGroupDO> newList = BeanUtil.copyToList(voList, UserGroupDO.class);
        this.buildIdMap(idMap, entities, newList);
        return idMap;
    }

    /**
     * 获取名称是否已存在.
     *
     * @param name  名称
     * @param orgId 组织主键（可选）
     */
    public void getNameIsExist(String name, Long orgId) {
        QueryWrapper<UserGroupDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserGroupDO::getName, name);
        if (Objects.nonNull(orgId)) {
            queryWrapper.lambda().eq(UserGroupDO::getOrgId, orgId);
        }
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 获取编码是否已存在.
     *
     * @param code  编码
     * @param orgId 组织主键（可选）
     */
    public void getCodeIsExist(String code, Long orgId) {
        QueryWrapper<UserGroupDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserGroupDO::getCode, code);
        queryWrapper.lambda().eq(UserGroupDO::getOrgId, orgId);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId 组织主键
     * @return Integer
     */
    public Integer getNextSeq(Long orgId) {
        QueryWrapper<UserGroupDO> queryWrapper = new QueryWrapper<>();
        if (Objects.nonNull(orgId)) {
            queryWrapper.lambda().eq(UserGroupDO::getOrgId, orgId);
        }
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    private void buildInitList(Long orgId, List<UserGroupDO> entities) {
        entities.forEach(entity -> {
            entity.setId(null);
            entity.setVersion(0);
            entity.setOrgId(orgId);
            entity.setRemark(MgtConstant.REMARK_INIT);
        });
    }

    private void buildIdMap(Map<Long, Long> idMap, List<UserGroupDO> oldList, List<UserGroupDO> newList) {
        oldList.forEach(oldData -> newList.forEach(newData -> {
            if (oldData.getName().equals(newData.getName())) {
                idMap.put(oldData.getId(), newData.getId());
            }
        }));
    }
}
