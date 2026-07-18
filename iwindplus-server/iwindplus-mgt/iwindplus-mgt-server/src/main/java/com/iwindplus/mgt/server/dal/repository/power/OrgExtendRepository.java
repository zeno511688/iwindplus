/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.dto.power.OrgExtendDTO;
import com.iwindplus.mgt.server.dal.mapper.power.OrgExtendMapper;
import com.iwindplus.mgt.server.dal.model.power.OrgExtendDO;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织扩展聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class OrgExtendRepository extends JoinCrudRepository<OrgExtendMapper, OrgExtendDO> {

    /**
     * 保存.
     *
     * @param entity 实体对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(OrgExtendDTO entity) {
        final OrgExtendDO model = BeanUtil.copyProperties(entity, OrgExtendDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param entity 实体对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(OrgExtendDTO entity) {
        if (CharSequenceUtil.isBlank(entity.getIntro())) {
            return Boolean.FALSE;
        }
        OrgExtendDO data = super.getOne(Wrappers.lambdaQuery(OrgExtendDO.class)
            .eq(OrgExtendDO::getOrgId, entity.getOrgId()));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final OrgExtendDO model = BeanUtil.copyProperties(entity, OrgExtendDO.class);
        model.setId(data.getId());
        super.updateById(model);
        return Boolean.TRUE;
    }

    /**
     * 查询简介.
     *
     * @param orgId 组织主键
     * @return String
     */
    public String getIntroByOrgId(Long orgId) {
        final List<OrgExtendDO> list = super.list(Wrappers.lambdaQuery(OrgExtendDO.class)
            .eq(OrgExtendDO::getOrgId, orgId).select(OrgExtendDO::getIntro));
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0).getIntro();
        }
        return null;
    }
}
