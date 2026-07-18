/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.service.impl.EsBaseServiceImpl;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.log.domain.dto.MailCaptchaLogDTO;
import com.iwindplus.log.domain.dto.MailCaptchaLogSearchDTO;
import com.iwindplus.log.domain.dto.MailSendValidDTO;
import com.iwindplus.log.domain.enums.LogCodeEnum;
import com.iwindplus.log.domain.enums.LogCodePrefixEnum;
import com.iwindplus.log.domain.vo.MailCaptchaLogPageVO;
import com.iwindplus.log.domain.vo.MailCaptchaLogVO;
import com.iwindplus.log.server.dal.model.MailCaptchaLogDO;
import com.iwindplus.log.server.service.MailCaptchaLogService;
import com.iwindplus.mgt.client.power.UserClient;
import com.iwindplus.mgt.domain.dto.power.UserBaseQueryDTO;
import com.iwindplus.mgt.domain.vo.power.UserInfoVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱验证码日志业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {"mailCaptchaLog"})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailCaptchaLogServiceImpl extends EsBaseServiceImpl<MailCaptchaLogDO>
    implements MailCaptchaLogService {

    private final UserClient userClient;
    private final RedissonService redissonService;

    @Override
    public String save(MailCaptchaLogDTO entity) {
        entity.setUsed(false);
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(this.redissonService.serialNum().getSerialNumDate(LogCodePrefixEnum.MAIL_PREFIX.getValue()));
        }
        final MailCaptchaLogDO model = BeanUtil.copyProperties(entity, MailCaptchaLogDO.class);
        super.save(model);
        entity.setId(model.getId());
        return entity.getId();
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeByIds(List<String> ids) {
        List<MailCaptchaLogDO> data = super.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        super.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    @Override
    public boolean removeExpireData() {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.lt(MailCaptchaLogDO::getExpireTime, LocalDateTime.now());
        return super.remove(wrapper);
    }

    @Override
    public IPage<MailCaptchaLogPageVO> page(MailCaptchaLogSearchDTO entity) {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = new EsLambdaQueryWrapper<>();
        final PageDTO<MailCaptchaLogDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        if (CharSequenceUtil.isNotBlank(entity.getRequestId())) {
            wrapper.eq(MailCaptchaLogDO::getRequestId, entity.getRequestId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            wrapper.eq(MailCaptchaLogDO::getBizNumber, entity.getBizNumber());
        }
        if (CharSequenceUtil.isNotBlank(entity.getTplCode())) {
            wrapper.eq(MailCaptchaLogDO::getTplCode, entity.getTplCode());
        }
        if (Objects.nonNull(entity.getOrgId())) {
            wrapper.eq(MailCaptchaLogDO::getOrgId, entity.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank((entity.getMail()))) {
            wrapper.like(MailCaptchaLogDO::getMail, entity.getMail());
        }
        if (CharSequenceUtil.isNotBlank(entity.getJobNumber())) {
            Long userId = GatewayLogServiceImpl.getUserIdByJobNumber(userClient, entity.getJobNumber());
            entity.setUserId(userId);
        } else if (CharSequenceUtil.isNotBlank(entity.getMobile())) {
            Long userId = GatewayLogServiceImpl.getUserIdByMobile(userClient, entity.getMobile());
            entity.setUserId(userId);
        }
        if (Objects.nonNull(entity.getUserId())) {
            wrapper.eq(MailCaptchaLogDO::getUserId, entity.getUserId());
        }
        List<OrderItem> orders = page.getOrders();
        if (CollUtil.isEmpty(orders)) {
            orders = new ArrayList<>(10);
            OrderItem orderItem = OrderItem.desc("modifiedTimestamp");
            orders.add(orderItem);
            page.setOrders(orders);
        }
        final IPage<MailCaptchaLogDO> modelPage = super.page(page, wrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, MailCaptchaLogPageVO.class));
    }


    @Override
    public boolean checkCanSend(MailSendValidDTO entity) {
        final Long userId = entity.getUserId();
        final Long orgId = entity.getOrgId();
        final LocalDateTime now = LocalDateTime.now();
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(MailCaptchaLogDO::getUserId, userId)
            .eq(MailCaptchaLogDO::getOrgId, orgId)
            .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
            .gt(MailCaptchaLogDO::getExpireTime, now)
            .eq(MailCaptchaLogDO::getUsed, false);
        boolean exists = super.count(wrapper) > 0;
        if (exists) {
            throw new BizException(LogCodeEnum.CAPTCHA_NOT_EXPIRED);
        }
        // 限制每天发送次数.
        final Integer limitCountDay = entity.getLimitCountDay();
        if (Objects.nonNull(limitCountDay)) {
            LocalDateTime begin = DatesUtil.getTimesMorning();
            LocalDateTime end = DatesUtil.getTimesNight();
            final EsLambdaQueryWrapper<MailCaptchaLogDO> dayWrapper = new EsLambdaQueryWrapper<>();
            dayWrapper.eq(MailCaptchaLogDO::getUserId, userId)
                .eq(MailCaptchaLogDO::getOrgId, orgId)
                .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
                .between(MailCaptchaLogDO::getCreatedTime, begin, end);
            long count = super.count(dayWrapper);
            if (count >= limitCountDay) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_DAY, new Object[]{limitCountDay});
            }
        }
        // 限制每小时发送条数.
        final Integer limitCountHour = entity.getLimitCountHour();
        if (Objects.nonNull(limitCountHour)) {
            LocalDateTime timeAgo = now.minusHours(1);
            final EsLambdaQueryWrapper<MailCaptchaLogDO> hourWrapper = new EsLambdaQueryWrapper<>();
            hourWrapper.eq(MailCaptchaLogDO::getUserId, userId)
                .eq(MailCaptchaLogDO::getOrgId, orgId)
                .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
                .ge(MailCaptchaLogDO::getCreatedTime, timeAgo);
            long count = super.count(hourWrapper);
            if (count >= limitCountHour) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_HOUR, new Object[]{limitCountHour});
            }
        }
        // 限制每分钟发送条数.
        final Integer limitCountMinute = entity.getLimitCountMinute();
        if (Objects.nonNull(limitCountMinute)) {
            LocalDateTime timeAgo = now.minusMinutes(1);
            final EsLambdaQueryWrapper<MailCaptchaLogDO> minuteWrapper = new EsLambdaQueryWrapper<>();
            minuteWrapper.eq(MailCaptchaLogDO::getUserId, userId)
                .eq(MailCaptchaLogDO::getOrgId, orgId)
                .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
                .ge(MailCaptchaLogDO::getCreatedTime, timeAgo);
            long count = super.count(minuteWrapper);
            if (count >= limitCountMinute) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_MINUTE, new Object[]{limitCountMinute});
            }
        }
        return Boolean.TRUE;
    }


    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public MailCaptchaLogVO getDetail(String id) {
        MailCaptchaLogDO data = super.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, MailCaptchaLogVO.class);
    }

    @Override
    public boolean validate(String tplCode, String mail, String captcha) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mail(mail).build();
        final ResultVO<UserInfoVO> userResponse = this.userClient.getLoginInfoByCondition(entity);
        userResponse.errorThrow();
        final UserInfoVO user = userResponse.getBizData();
        final Long userId = user.getUserId();
        final Long orgId = user.getOrgId();

        return this.validateByUserId(tplCode, userId, orgId, captcha);
    }

    @Override
    public boolean validateByUserId(String tplCode, Long userId, Long orgId, String captcha) {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = new EsLambdaQueryWrapper<>();
        wrapper.eq(MailCaptchaLogDO::getUserId, userId)
            .eq(MailCaptchaLogDO::getOrgId, orgId)
            .eq(MailCaptchaLogDO::getCaptcha, captcha.trim())
            .eq(MailCaptchaLogDO::getTplCode, tplCode)
            .orderByDesc(MailCaptchaLogDO::getModifiedTimestamp)
            .limit(1);
        return this.checkCaptcha(wrapper);
    }

    private boolean checkCaptcha(EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper) {
        MailCaptchaLogDO data = super.getOne(wrapper);
        if (Objects.isNull(data)) {
            throw new BizException(LogCodeEnum.CAPTCHA_ERROR);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(data.getExpireTime())) {
            throw new BizException(LogCodeEnum.CAPTCHA_EXPIRED);
        }
        if (Boolean.TRUE.equals(data.getUsed())) {
            throw new BizException(LogCodeEnum.CAPTCHA_CAN_USE_ONCE);
        }
        MailCaptchaLogDO update = new MailCaptchaLogDO();
        update.setId(data.getId());
        update.setUsed(true);
        update.setUseTime(now);
        super.updateById(update);
        return true;
    }
}
