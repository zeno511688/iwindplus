/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.dto.power.OrgAuditDTO;
import com.iwindplus.mgt.domain.vo.power.OrgAuditVO;
import com.iwindplus.mgt.server.dal.mapper.power.OrgAuditMapper;
import com.iwindplus.mgt.server.dal.model.power.OrgAuditDO;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织审核聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class OrgAuditRepository extends JoinCrudRepository<OrgAuditMapper, OrgAuditDO> {

    /**
     * 保存.
     *
     * @param entity 实体对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(OrgAuditDTO entity) {
        OrgAuditVO data = super.baseMapper.selectNewestByOrgId(entity.getOrgId());
        if (Objects.nonNull(data) && entity.getAuditStatus().equals(data.getAuditStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        entity.setSeq(this.getNextSeq(entity.getOrgId()));
        entity.setRemark(entity.getAuditStatus().getDesc());
        final OrgAuditDO model = BeanUtil.copyProperties(entity, OrgAuditDO.class);
        super.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    /**
     * 查询下一个排序号.
     *
     * @param orgId 组织主键
     * @return Integer
     */
    public Integer getNextSeq(Long orgId) {
        QueryWrapper<OrgAuditDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OrgAuditDO::getOrgId, orgId);
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }
}
