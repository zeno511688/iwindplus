/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.infrastructure.persistence.upms.organization;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.api.upms.vo.OrgBaseCheckedVO;
import com.iwindplus.mgt.application.query.upms.organization.dto.OrgSearchDTO;
import com.iwindplus.mgt.application.query.upms.organization.vo.OrgPageVO;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 组织聚合问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class OrgRepository extends JoinCrudRepository<OrgMapper, OrgDO> {

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<OrgPageVO> page(OrgSearchDTO entity) {
        final PageDTO<OrgDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<OrgDO> queryWrapper = Wrappers.lambdaQuery(OrgDO.class)
            .orderByDesc(OrgDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(OrgDO::getStatus, entity.getStatus());
        }
        if (Objects.nonNull(entity.getAuditStatus())) {
            queryWrapper.eq(OrgDO::getAuditStatus, entity.getAuditStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(OrgDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(OrgDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getAbbr())) {
            queryWrapper.eq(OrgDO::getAbbr, entity.getAbbr().trim());
        }
        queryWrapper.select(OrgDO::getId, OrgDO::getCreatedTimestamp, OrgDO::getCreatedBy,
            OrgDO::getModifiedTimestamp, OrgDO::getModifiedBy, OrgDO::getVersion, OrgDO::getStatus, OrgDO::getAuditStatus, OrgDO::getCode,
            OrgDO::getName, OrgDO::getAbbr, OrgDO::getUscc, OrgDO::getCountry, OrgDO::getProvince, OrgDO::getCity, OrgDO::getDistrict,
            OrgDO::getBuildInFlag
        );
        final PageDTO<OrgDO> modelPage = super.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, OrgPageVO.class));
    }

    /**
     * 检查名称是否存在.
     *
     * @param name 名称
     */
    public void getNameIsExist(String name) {
        final LambdaQueryWrapper<OrgDO> queryWrapper = Wrappers.lambdaQuery(OrgDO.class)
            .eq(OrgDO::getName, name);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.NAME_EXIST);
        }
    }

    /**
     * 检查编码是否存在.
     *
     * @param code 编码
     */
    public void getCodeIsExist(String code) {
        final LambdaQueryWrapper<OrgDO> queryWrapper = Wrappers.lambdaQuery(OrgDO.class)
            .eq(OrgDO::getCode, code);
        boolean result = SqlHelper.retBool(super.count(queryWrapper));
        if (Boolean.TRUE.equals(result)) {
            throw new BizException(MgtCodeEnum.CODE_EXIST);
        }
    }

    /**
     * 查询下一个排序号.
     *
     * @return Integer
     */
    public Integer getNextSeq() {
        QueryWrapper<OrgDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(seq) as seq");
        Function<Object, Integer> function = val -> Integer.valueOf(val.toString());
        Integer data = super.getObj(queryWrapper, function);
        return Optional.ofNullable(data).map(x -> x + 1).orElse(1);
    }

    /**
     * 查询用户组织.
     *
     * @param userId 用户主键
     * @return OrgBaseCheckedVO
     */
    public OrgBaseCheckedVO getOrg(Long userId) {
        List<OrgBaseCheckedVO> list = super.baseMapper.selectListByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        List<OrgBaseCheckedVO> resultList = list.stream().filter(OrgBaseCheckedVO::getChecked).collect(Collectors.toCollection(ArrayList::new));
        if (CollUtil.isEmpty(resultList)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        return resultList.get(0);
    }

    /**
     * 查询用户组织主键.
     *
     * @param userId 用户主键
     * @return Long
     */
    public Long getOrgId(Long userId) {
        final OrgBaseCheckedVO org = this.getOrg(userId);
        if (Objects.isNull(org)) {
            throw new BizException(MgtCodeEnum.ORG_NOT_EXIST);
        }
        return org.getId();
    }
}
