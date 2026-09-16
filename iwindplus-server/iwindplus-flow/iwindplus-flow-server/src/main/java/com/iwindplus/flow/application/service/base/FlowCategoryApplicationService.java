/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.service.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.flow.application.service.base.dto.FlowCategoryEditDTO;
import com.iwindplus.flow.application.service.base.dto.FlowCategorySaveDTO;
import com.iwindplus.flow.common.constant.FlowConstant.RedisCacheConstant;
import com.iwindplus.flow.common.enums.FlowCodePrefixEnum;
import com.iwindplus.flow.infrastructure.persistence.base.FlowCategoryDO;
import com.iwindplus.flow.infrastructure.persistence.base.FlowCategoryRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程分类业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_FLOW_CATEGORY})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowCategoryApplicationService {

    private final RedissonExecutor redissonExecutor;
    private final FlowCategoryRepository flowCategoryRepository;

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean save(FlowCategorySaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        this.flowCategoryRepository.getNameIsExist(entity.getName());
        entity.setSeq(this.flowCategoryRepository.getNextSeq());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(FlowCodePrefixEnum.CATEGORY_PREFIX.code()));
        }
        this.flowCategoryRepository.getCodeIsExist(entity.getCode());
        final FlowCategoryDO model = BeanUtil.copyProperties(entity, FlowCategoryDO.class);
        this.flowCategoryRepository.save(model);
        return Boolean.TRUE;
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<FlowCategoryDO> list = this.flowCategoryRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.flowCategoryRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean edit(FlowCategoryEditDTO entity) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.flowCategoryRepository.getNameIsExist(entity.getName().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowCategoryDO model = BeanUtil.copyProperties(entity, FlowCategoryDO.class);
        this.flowCategoryRepository.updateById(model);
        return Boolean.TRUE;
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        FlowCategoryDO param = new FlowCategoryDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.flowCategoryRepository.updateById(param);
        return Boolean.TRUE;
    }

    /**
     * 编辑设为内置.
     *
     * @param id          主键
     * @param buildInFlag 是否内置
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        FlowCategoryDO data = this.flowCategoryRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        FlowCategoryDO param = new FlowCategoryDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.flowCategoryRepository.updateById(param);
        return Boolean.TRUE;
    }

}
