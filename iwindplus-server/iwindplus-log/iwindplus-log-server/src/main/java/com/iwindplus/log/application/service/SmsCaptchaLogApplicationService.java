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
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.support.EsLambdaQueryWrapper;
import com.iwindplus.base.es.support.EsWrappers;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.log.api.dto.SmsCaptchaLogDTO;
import com.iwindplus.log.api.dto.SmsSendValidDTO;
import com.iwindplus.log.common.constant.LogConstant.RedisCacheConstant;
import com.iwindplus.log.common.enums.LogCodeEnum;
import com.iwindplus.log.common.enums.LogCodePrefixEnum;
import com.iwindplus.log.infrastructure.persistence.captcha.SmsCaptchaLogDO;
import com.iwindplus.log.infrastructure.persistence.captcha.SmsCaptchaLogRepository;
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
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短信验证码日志业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SMS_CAPTCHA_LOG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SmsCaptchaLogApplicationService {

    private final SmsCaptchaLogRepository smsCaptchaLogRepository;
    private final UserClient userClient;
    private final RedissonExecutor redissonExecutor;

    /**
     * 添加.
     *
     * @param entity   对象
     * @return String
     */
    public String save(SmsCaptchaLogDTO entity) {
        final Map<String, String> headerMap = HeaderContextHolder.getContext();
        if (CharSequenceUtil.isBlank(entity.getRequestId())) {
            String requestId = Optional.ofNullable(headerMap)
                .map(map -> map.get(HeaderConstant.X_REQUESTED_ID))
                .filter(CharSequenceUtil::isNotBlank)
                .orElseGet(() -> MDC.get(HeaderConstant.X_REQUESTED_ID));
            entity.setRequestId(requestId);
        }
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(IdUtil.simpleUUID());
        }
        entity.setUsed(false);
        if (ObjectUtil.isEmpty(entity.getBizNumber())) {
            entity.setBizNumber(this.redissonExecutor.serialNum().getSerialNumDate(LogCodePrefixEnum.SMS_PREFIX.getValue()));
        }
        final SmsCaptchaLogDO model = BeanUtil.copyProperties(entity, SmsCaptchaLogDO.class);
        this.smsCaptchaLogRepository.save(model);
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
        List<SmsCaptchaLogDO> data = this.smsCaptchaLogRepository.listById(ids);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.smsCaptchaLogRepository.removeByIds(ids, false);
        return Boolean.TRUE;
    }

    /**
     * 清理过期的数据.
     *
     * @return boolean
     */
    @CacheEvict(allEntries = true)
    public boolean removeExpireData() {
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> queryWrapper = EsWrappers.<SmsCaptchaLogDO>lambdaQuery()
            .lt(SmsCaptchaLogDO::getExpireTime, System.currentTimeMillis());
        return this.smsCaptchaLogRepository.remove(queryWrapper);
    }

    /**
     * 校验是否可以发送.
     *
     * @param entity 对象
     * @return boolean
     */
    public boolean checkCanSend(SmsSendValidDTO entity) {
        final Long userId = entity.getUserId();
        final Long orgId = entity.getOrgId();
        final LocalDateTime now = LocalDateTime.now();
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper = EsWrappers.<SmsCaptchaLogDO>lambdaQuery()
            .eq(SmsCaptchaLogDO::getUserId, userId)
            .eq(SmsCaptchaLogDO::getOrgId, orgId)
            .eq(SmsCaptchaLogDO::getTplCode, entity.getTplCode())
            .gt(SmsCaptchaLogDO::getExpireTime, System.currentTimeMillis())
            .eq(SmsCaptchaLogDO::getUsed, false);
        boolean exists = this.smsCaptchaLogRepository.count(wrapper) > 0;
        if (exists) {
            throw new BizException(LogCodeEnum.CAPTCHA_NOT_EXPIRED);
        }

        // 限制每天发送次数.
        final Integer limitCountDay = entity.getLimitCountDay();
        if (Objects.nonNull(limitCountDay)) {
            long begin = toTimestamp(now.toLocalDate().atStartOfDay());
            long end = toTimestamp(now.toLocalDate().atTime(LocalTime.MAX));
            final EsLambdaQueryWrapper<SmsCaptchaLogDO> dayWrapper = EsWrappers.<SmsCaptchaLogDO>lambdaQuery()
                .eq(SmsCaptchaLogDO::getUserId, userId)
                .eq(SmsCaptchaLogDO::getOrgId, orgId)
                .eq(SmsCaptchaLogDO::getTplCode, entity.getTplCode())
                .between(SmsCaptchaLogDO::getCreatedTimestamp, begin, end);
            long count = this.smsCaptchaLogRepository.count(dayWrapper);
            if (count >= limitCountDay) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_DAY, new Object[]{limitCountDay});
            }
        }
        // 限制每小时发送条数.
        final Integer limitCountHour = entity.getLimitCountHour();
        if (Objects.nonNull(limitCountHour)) {
            long begin = toTimestamp(now.minusHours(1));
            final EsLambdaQueryWrapper<SmsCaptchaLogDO> hourWrapper = EsWrappers.<SmsCaptchaLogDO>lambdaQuery()
                .eq(SmsCaptchaLogDO::getUserId, userId)
                .eq(SmsCaptchaLogDO::getOrgId, orgId)
                .eq(SmsCaptchaLogDO::getTplCode, entity.getTplCode())
                .ge(SmsCaptchaLogDO::getCreatedTimestamp, begin);
            long count = this.smsCaptchaLogRepository.count(hourWrapper);
            if (count >= limitCountHour) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_HOUR, new Object[]{limitCountHour});
            }
        }
        // 限制每分钟发送条数.
        final Integer limitCountMinute = entity.getLimitCountMinute();
        if (Objects.nonNull(limitCountMinute)) {
            long begin = toTimestamp(now.minusMinutes(1));
            final EsLambdaQueryWrapper<SmsCaptchaLogDO> minuteWrapper = EsWrappers.<SmsCaptchaLogDO>lambdaQuery()
                .eq(SmsCaptchaLogDO::getUserId, userId)
                .eq(SmsCaptchaLogDO::getOrgId, orgId)
                .eq(SmsCaptchaLogDO::getTplCode, entity.getTplCode())
                .ge(SmsCaptchaLogDO::getCreatedTimestamp, begin);
            long count = this.smsCaptchaLogRepository.count(minuteWrapper);
            if (count >= limitCountMinute) {
                throw new BizException(LogCodeEnum.CAPTCHA_LIMIT_MINUTE, new Object[]{limitCountMinute});
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 校验验证码（手机）.
     *
     * @param tplCode 模板配置编码
     * @param mobile  手机
     * @param captcha 验证码
     * @return boolean
     */
    public boolean validate(String tplCode, String mobile, String captcha) {
        final UserBaseQueryDTO entity = UserBaseQueryDTO.builder().mobile(mobile).build();
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
        final EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper = EsWrappers.<SmsCaptchaLogDO>lambdaQuery()
            .eq(SmsCaptchaLogDO::getUserId, userId)
            .eq(SmsCaptchaLogDO::getOrgId, orgId)
            .eq(SmsCaptchaLogDO::getCaptcha, captcha.trim())
            .eq(SmsCaptchaLogDO::getTplCode, tplCode)
            .orderByDesc(SmsCaptchaLogDO::getModifiedTimestamp)
            .limit(1);
        return this.checkCaptcha(wrapper);
    }

    private boolean checkCaptcha(EsLambdaQueryWrapper<SmsCaptchaLogDO> wrapper) {
        SmsCaptchaLogDO data = this.smsCaptchaLogRepository.getOne(wrapper);
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
        SmsCaptchaLogDO build = new SmsCaptchaLogDO();
        build.setId(data.getId());
        build.setUsed(true);
        build.setUseTime(now);
        this.smsCaptchaLogRepository.updateById(build);
        return Boolean.TRUE;
    }

    private long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }
}
