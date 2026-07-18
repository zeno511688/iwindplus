/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.setup.domain.constant.SetupConstant.RedisCacheConstant;
import com.iwindplus.setup.domain.dto.MailTplEditDTO;
import com.iwindplus.setup.domain.dto.MailTplSaveDTO;
import com.iwindplus.setup.domain.dto.MailTplSearchDTO;
import com.iwindplus.setup.domain.enums.SetupCodeEnum;
import com.iwindplus.setup.domain.vo.MailTplBaseVO;
import com.iwindplus.setup.domain.vo.MailTplPageVO;
import com.iwindplus.setup.domain.vo.MailTplVO;
import com.iwindplus.setup.server.dal.model.MailTplDO;
import com.iwindplus.setup.server.dal.repository.MailTplRepository;
import com.iwindplus.setup.server.service.MailTplService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱模板业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_TPL})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailTplServiceImpl implements MailTplService {

    private final MailTplRepository mailTplRepository;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(MailTplSaveDTO entity) {
        this.mailTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());

        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        String code = IdUtil.simpleUUID();
        entity.setCode(code);
        final MailTplDO model = BeanUtil.copyProperties(entity, MailTplDO.class);
        this.buildDefault(model);
        this.mailTplRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<MailTplDO> list = this.mailTplRepository.listByIds(ids);
        if (Objects.isNull(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(MailTplDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.mailTplRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean edit(MailTplEditDTO entity) {
        // 编辑
        MailTplDO data = this.mailTplRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.mailTplRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.mailTplRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final MailTplDO model = BeanUtil.copyProperties(entity, MailTplDO.class);
        this.buildDefault(model);
        this.mailTplRepository.updateById(model);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        MailTplDO data = this.mailTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MailTplDO entity = new MailTplDO();
        entity.setId(id);
        entity.setStatus(status);
        entity.setVersion(data.getVersion());
        this.mailTplRepository.updateById(entity);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        MailTplDO data = this.mailTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        MailTplDO param = new MailTplDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.mailTplRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<MailTplPageVO> page(PageDTO<MailTplDO> page, MailTplSearchDTO entity) {
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        final LambdaQueryWrapper<MailTplDO> queryWrapper = Wrappers.lambdaQuery(MailTplDO.class);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(MailTplDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(MailTplDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(MailTplDO::getName, entity.getName().trim());
        }
        // 排序
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem item = OrderItem.desc(CommonConstant.DbConstant.MODIFIED_TIME);
            orders.add(item);
        }
        orders.forEach(order -> {
            String column = order.getColumn();
            String underline = CharSequenceUtil.toUnderlineCase(column);
            order.setColumn(underline);
        });
        page.setOrders(orders);
        queryWrapper.select(MailTplDO::getId, MailTplDO::getCreatedTime, MailTplDO::getCreatedTimestamp, MailTplDO::getCreatedBy,
            MailTplDO::getModifiedTime, MailTplDO::getModifiedTimestamp, MailTplDO::getModifiedBy, MailTplDO::getVersion, MailTplDO::getRemark,
            MailTplDO::getBuildInFlag, MailTplDO::getStatus, MailTplDO::getCode, MailTplDO::getName
        );
        final PageDTO<MailTplDO> modelPage = this.mailTplRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailTplPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MailTplVO getByCode(String code) {
        MailTplDO data = this.mailTplRepository.getOne(Wrappers.lambdaQuery(MailTplDO.class)
            .eq(MailTplDO::getCode, code.trim()));
        if (Objects.isNull(data)) {
            throw new BizException(SetupCodeEnum.MAIL_TEMPLATE_NOT_EXIST);
        }
        if (EnableStatusEnum.DISABLE == data.getStatus()) {
            throw new BizException(SetupCodeEnum.MAIL_TEMPLATE_DISABLED);
        } else if (EnableStatusEnum.LOCKED == data.getStatus()) {
            throw new BizException(SetupCodeEnum.MAIL_TEMPLATE_LOCKED);
        }
        final MailTplVO result = BeanUtil.copyProperties(data, MailTplVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MailTplVO getDetail(Long id) {
        MailTplDO data = this.mailTplRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final MailTplVO result = BeanUtil.copyProperties(data, MailTplVO.class);
        return result;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<MailTplBaseVO> listEnabled() {
        final List<MailTplDO> list = this.mailTplRepository.list(Wrappers.lambdaQuery(MailTplDO.class)
            .eq(MailTplDO::getStatus, EnableStatusEnum.ENABLE)
            .orderByDesc(MailTplDO::getCreatedTime));
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return BeanUtil.copyToList(list, MailTplBaseVO.class);
    }

    private void buildDefault(MailTplDO entity) {
        if (Objects.isNull(entity.getCaptchaLength())) {
            entity.setCaptchaLength(SmsConstant.CAPTCHA_LENGTH);
        }
        if (Objects.isNull(entity.getCaptchaTimeout())) {
            entity.setCaptchaTimeout(SmsConstant.CAPTCHA_TIMEOUT);
        }
        if (Objects.isNull(entity.getLimitCountDay())) {
            entity.setLimitCountDay(NumberConstant.NUMBER_TWENTY);
        }
        if (Objects.isNull(entity.getLimitCountHour())) {
            entity.setLimitCountHour(NumberConstant.NUMBER_FIVE);
        }
        if (Objects.isNull(entity.getLimitCountMinute())) {
            entity.setLimitCountMinute(NumberConstant.NUMBER_ONE);
        }
    }
}
