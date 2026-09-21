/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.application.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.base.util.MdcUtil;
import com.iwindplus.log.api.dto.MailCaptchaLogDTO;
import com.iwindplus.log.api.dto.MailSendValidDTO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.common.enums.LogCodeEnum;
import com.iwindplus.log.common.enums.LogCodePrefixEnum;
import com.iwindplus.log.infrastructure.persistence.captcha.MailCaptchaLogDO;
import com.iwindplus.log.infrastructure.persistence.captcha.MailCaptchaLogRepository;
import com.iwindplus.mgt.api.upms.dto.UserBaseQueryDTO;
import com.iwindplus.mgt.api.upms.vo.UserInfoVO;
import com.iwindplus.mgt.client.upms.UserClient;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱验证码日志业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_MAIL_CAPTCHA_LOG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MailCaptchaLogApplicationService {

    private final MailCaptchaLogRepository mailCaptchaLogRepository;
    private final UserClient userClient;
    private final RedissonExecutor redissonExecutor;

    /**
     * 添加.
     *
     * @param entity   对象
     * @return String
     */
    public String save(MailCaptchaLogDTO entity) {
        final Map<String, String> headerMap = HeaderContextHolder.getContext();
        if (CharSequenceUtil.isBlank(entity.getRequestId())) {
            String requestId = Optional.ofNullable(headerMap)
                .map(map -> map.get(HeaderConstant.X_REQUESTED_ID))
                .orElse(MdcUtil.get(HeaderConstant.X_REQUESTED_ID));
            entity.setRequestId(requestId);
        }
        entity.setUsed(false);
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(this.redissonExecutor.serialNum().getSerialNumDate(LogCodePrefixEnum.MAIL_PREFIX.getValue()));
        }
        final MailCaptchaLogDO model = BeanUtil.copyProperties(entity, MailCaptchaLogDO.class);
        this.mailCaptchaLogRepository.save(model);
        entity.setId(model.getId());
        return entity.getId();
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<String> ids) {
        List<MailCaptchaLogDO> data = this.mailCaptchaLogRepository.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.mailCaptchaLogRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    /**
     * 清理过期的数据.
     *
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeExpireData() {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = EsWrappers.<MailCaptchaLogDO>lambdaQuery()
            .lt(MailCaptchaLogDO::getExpireTime, System.currentTimeMillis());
        return this.mailCaptchaLogRepository.remove(wrapper);
    }

    /**
     * 校验是否可以发送.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean checkCanSend(MailSendValidDTO entity) {
        final Long userId = entity.getUserId();
        final Long orgId = entity.getOrgId();
        final LocalDateTime now = LocalDateTime.now();
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = EsWrappers.<MailCaptchaLogDO>lambdaQuery()
            .eq(MailCaptchaLogDO::getUserId, userId)
            .eq(MailCaptchaLogDO::getOrgId, orgId)
            .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
            .gt(MailCaptchaLogDO::getExpireTime, System.currentTimeMillis())
            .eq(MailCaptchaLogDO::getUsed, false);
        boolean exists = this.mailCaptchaLogRepository.count(wrapper) > 0;
        if (exists) {
            throw new BizException(LogCodeEnum.CAPTCHA_NOT_EXPIRED);
        }
        // 限制每天发送次数.
        final Integer limitCountDay = entity.getLimitCountDay();
        if (Objects.nonNull(limitCountDay)) {
            long begin = toTimestamp(now.toLocalDate().atStartOfDay());
            long end = toTimestamp(now.toLocalDate().atTime(LocalTime.MAX));
            final EsLambdaQueryWrapper<MailCaptchaLogDO> dayWrapper = EsWrappers.<MailCaptchaLogDO>lambdaQuery()
                .eq(MailCaptchaLogDO::getUserId, userId)
                .eq(MailCaptchaLogDO::getOrgId, orgId)
                .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
                .between(MailCaptchaLogDO::getCreatedTimestamp, begin, end);
            long count = this.mailCaptchaLogRepository.count(dayWrapper);
            if (count >= limitCountDay) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_DAY, new Object[]{limitCountDay});
            }
        }
        // 限制每小时发送条数.
        final Integer limitCountHour = entity.getLimitCountHour();
        if (Objects.nonNull(limitCountHour)) {
            long begin = toTimestamp(now.minusHours(1));
            final EsLambdaQueryWrapper<MailCaptchaLogDO> hourWrapper = EsWrappers.<MailCaptchaLogDO>lambdaQuery()
                .eq(MailCaptchaLogDO::getUserId, userId)
                .eq(MailCaptchaLogDO::getOrgId, orgId)
                .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
                .ge(MailCaptchaLogDO::getCreatedTimestamp, begin);
            long count = this.mailCaptchaLogRepository.count(hourWrapper);
            if (count >= limitCountHour) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_HOUR, new Object[]{limitCountHour});
            }
        }
        // 限制每分钟发送条数.
        final Integer limitCountMinute = entity.getLimitCountMinute();
        if (Objects.nonNull(limitCountMinute)) {
            long begin = toTimestamp(now.minusMinutes(1));
            final EsLambdaQueryWrapper<MailCaptchaLogDO> minuteWrapper = EsWrappers.<MailCaptchaLogDO>lambdaQuery()
                .eq(MailCaptchaLogDO::getUserId, userId)
                .eq(MailCaptchaLogDO::getOrgId, orgId)
                .eq(MailCaptchaLogDO::getTplCode, entity.getTplCode())
                .ge(MailCaptchaLogDO::getCreatedTimestamp, begin);
            long count = this.mailCaptchaLogRepository.count(minuteWrapper);
            if (count >= limitCountMinute) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_MINUTE, new Object[]{limitCountMinute});
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 校验验证码（邮箱）.
     *
     * @param tplCode 模板配置编码
     * @param mail    邮箱
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validate(String tplCode, String mail, String captcha) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mail(mail).build();
        final ResultVO<UserInfoVO> userResponse = this.userClient.getLoginInfoByCondition(entity);
        userResponse.errorThrow();
        final UserInfoVO user = userResponse.getBizData();
        final Long userId = user.getUserId();
        final Long orgId = user.getOrgId();

        return this.validateByUserId(tplCode, userId, orgId, captcha);
    }

    /**
     * 校验验证码（用户主键）.
     *
     * @param tplCode 模板配置编码
     * @param userId  用户主键
     * @param orgId   组织主键
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validateByUserId(String tplCode, Long userId, Long orgId, String captcha) {
        final EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper = EsWrappers.<MailCaptchaLogDO>lambdaQuery()
            .eq(MailCaptchaLogDO::getUserId, userId)
            .eq(MailCaptchaLogDO::getOrgId, orgId)
            .eq(MailCaptchaLogDO::getCaptcha, captcha.trim())
            .eq(MailCaptchaLogDO::getTplCode, tplCode)
            .orderByDesc(MailCaptchaLogDO::getModifiedTimestamp)
            .limit(1);
        return this.checkCaptcha(wrapper);
    }

    private boolean checkCaptcha(EsLambdaQueryWrapper<MailCaptchaLogDO> wrapper) {
        MailCaptchaLogDO data = this.mailCaptchaLogRepository.getOne(wrapper);
        if (Objects.isNull(data)) {
            throw new BizException(LogCodeEnum.CAPTCHA_ERROR);
        }
        Long now = System.currentTimeMillis();
        if (now > data.getExpireTime()) {
            throw new BizException(LogCodeEnum.CAPTCHA_EXPIRED);
        }
        if (Boolean.TRUE.equals(data.getUsed())) {
            throw new BizException(LogCodeEnum.CAPTCHA_CAN_USE_ONCE);
        }
        MailCaptchaLogDO update = new MailCaptchaLogDO();
        update.setId(data.getId());
        update.setUsed(true);
        update.setUseTime(now);
        this.mailCaptchaLogRepository.updateById(update);
        return true;
    }

    private long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }
}
