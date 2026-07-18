/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.dto.power.UserOrgDTO;
import com.iwindplus.mgt.server.dal.mapper.power.UserOrgMapper;
import com.iwindplus.mgt.server.dal.model.power.UserOrgDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户组织聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserOrgRepository extends JoinCrudRepository<UserOrgMapper, UserOrgDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(UserOrgDTO entity) {
        List<UserOrgDO> entities = new ArrayList<>(10);
        UserOrgDO model = BeanUtil.copyProperties(entity, UserOrgDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param orgId   组织主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUser(Long orgId, Set<Long> userIds) {
        if (Objects.isNull(orgId) || CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserOrgDO> entities = new ArrayList<>(10);
        userIds.stream().forEach(userId -> {
            UserOrgDO entity = UserOrgDO.builder()
                .userId(userId)
                .orgId(orgId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param userId 用户主键
     * @param orgIds 组织主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchOrg(Long userId, Set<Long> orgIds) {
        if (Objects.isNull(userId) || CollUtil.isEmpty(orgIds)) {
            return Boolean.FALSE;
        }
        List<UserOrgDO> entities = new ArrayList<>(10);
        orgIds.stream().forEach(orgId -> {
            UserOrgDO entity = UserOrgDO.builder()
                .userId(userId)
                .orgId(orgId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<UserOrgDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserOrgDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            UserOrgDO model = BeanUtil.copyProperties(entity, UserOrgDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            super.saveBatch(params, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 编辑.
     *
     * @param orgId   组织主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUser(Long orgId, Set<Long> userIds) {
        if (Objects.isNull(orgId)) {
            return Boolean.FALSE;
        }
        super.baseMapper.deleteByOrgIds(Arrays.asList(orgId));
        this.saveBatchUser(orgId, userIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param userId 用户主键
     * @param orgIds 组织主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchOrg(Long userId, Set<Long> orgIds) {
        if (Objects.isNull(userId)) {
            return Boolean.FALSE;
        }
        super.baseMapper.deleteByUserIds(Arrays.asList(userId));
        this.saveBatchOrg(userId, orgIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑设为选中.
     *
     * @param newUserOrgId 新选中用户组织关系主键
     * @param oldUserOrgId 旧选中用户组织关系主键
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editChecked(Long newUserOrgId, Long oldUserOrgId) {
        List<Long> ids = Lists.newArrayList(newUserOrgId, oldUserOrgId);
        List<UserOrgDO> list = super.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        Map<Long, Integer> idVersionMap = new HashMap<>(16);
        this.buildIdVersionMap(idVersionMap, list, ids);
        // 新组织
        UserOrgDO newUserOrg = new UserOrgDO();
        newUserOrg.setId(newUserOrgId);
        newUserOrg.setChecked(true);
        newUserOrg.setVersion(idVersionMap.get(newUserOrgId));
        super.updateById(newUserOrg);
        // 旧组织
        UserOrgDO oldUserOrg = new UserOrgDO();
        oldUserOrg.setId(oldUserOrgId);
        oldUserOrg.setChecked(false);
        oldUserOrg.setVersion(idVersionMap.get(oldUserOrgId));
        super.updateById(oldUserOrg);
        return Boolean.TRUE;
    }

    /**
     * 校验用户是否切换过组织.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return boolean
     */
    public boolean checkChangeOrg(Long userId, Long orgId) {
        final long count = super.count(Wrappers.lambdaQuery(UserOrgDO
            .builder()
            .userId(userId)
            .orgId(orgId)
            .checked(Boolean.TRUE)
            .build()));
        return SqlHelper.retBool(count);
    }

    /**
     * 校验是否选中.
     *
     * @param userId 用户主键
     * @return boolean
     */
    public boolean checkIsChecked(Long userId) {
        long count = super.count(Wrappers.lambdaQuery(UserOrgDO.class)
            .eq(UserOrgDO::getUserId, userId)
            .eq(UserOrgDO::getChecked, true));
        return SqlHelper.retBool(count);
    }

    private void buildParam(UserOrgDO entity, List<UserOrgDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(UserOrgDO.class)
            .eq(UserOrgDO::getUserId, entity.getUserId())
            .eq(UserOrgDO::getOrgId, entity.getOrgId()));
        if (!SqlHelper.retBool(count)) {
            entity.setChecked(!this.checkIsChecked(entity.getUserId()));
            entities.add(entity);
        }
    }

    private void buildIdVersionMap(Map<Long, Integer> idVersionMap, List<UserOrgDO> oldList, List<Long> idList) {
        oldList.forEach(oldData -> idList.forEach(newData -> {
            if (oldData.getId().equals(newData)) {
                idVersionMap.put(oldData.getId(), oldData.getVersion());
            }
        }));
    }
}
