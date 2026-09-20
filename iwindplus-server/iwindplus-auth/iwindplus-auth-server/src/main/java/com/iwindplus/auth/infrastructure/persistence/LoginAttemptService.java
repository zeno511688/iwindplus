/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.persistence;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty.LoginSecurityConfig;
import com.iwindplus.base.util.DatesUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 登录尝试服务，基于Redis跟踪登录失败次数和账号锁定状态.
 * <p>
 * 策略：
 * <ul>
 *   <li>错误次数达到 captchaThreshold 时，要求图形验证码</li>
 *   <li>错误次数达到 maxAttemptCount 时，按指数退避策略锁定账号</li>
 *   <li>登录成功后清除所有记录</li>
 * </ul>
 *
 * @author zengdegui
 * @since 2026/09/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthProperty authProperty;

    /**
     * 检查是否需要图形验证码.
     *
     * @param username 用户名
     * @return true表示需要验证码
     */
    public boolean needCaptcha(String username) {
        if (!this.isEnabled()) {
            return false;
        }
        Integer attemptCount = this.getAttemptCount(username);
        LoginSecurityConfig config = this.authProperty.getLoginSecurity();
        return attemptCount >= config.getCaptchaThreshold();
    }

    /**
     * 检查账号是否被锁定.
     * <p>
     * 如果被锁定，返回剩余锁定时间（毫秒）；如果未锁定，返回null.
     *
     * @param username 用户名
     * @return 剩余锁定时间（毫秒），null表示未锁定
     */
    public Long getRemainingLockTime(String username) {
        if (!this.isEnabled()) {
            return null;
        }
        String lockKey = this.buildLockKey(username);
        Long ttl = this.redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);
        if (Objects.nonNull(ttl) && ttl > 0) {
            return ttl;
        }
        return null;
    }

    /**
     * 记录登录失败.
     * <p>
     * 递增失败次数，如果超过最大次数则锁定账号.
     *
     * @param username 用户名
     * @return 当前失败次数
     */
    public Integer recordFailedAttempt(String username) {
        if (!this.isEnabled()) {
            return 0;
        }
        String attemptKey = this.buildAttemptKey(username);
        Integer attemptCount = (Integer) this.redisTemplate.opsForValue().get(attemptKey);
        attemptCount = Objects.nonNull(attemptCount) ? attemptCount + 1 : 1;

        LoginSecurityConfig config = this.authProperty.getLoginSecurity();
        // 失败次数缓存时间设为锁定最大时间的2倍，确保锁定期间记录不丢失
        long cacheSeconds = DatesUtil.calculateTotalSecondsByFrequency(config.getLockFrequency(), 1800) * 2;
        this.redisTemplate.opsForValue().set(attemptKey, attemptCount, cacheSeconds, TimeUnit.SECONDS);

        // 超过最大错误次数，锁定账号
        if (attemptCount >= config.getMaxAttemptCount()) {
            long baseTimeMillis = System.currentTimeMillis();
            // 重试次数从0开始，第maxAttemptCount次对应index=0
            int retryCount = attemptCount - config.getMaxAttemptCount();
            long lockUntil = DatesUtil.getNextRetryTime(baseTimeMillis, config.getLockFrequency(), retryCount);
            long lockSeconds = (lockUntil - baseTimeMillis) / 1000;
            if (lockSeconds > 0) {
                String lockKey = this.buildLockKey(username);
                this.redisTemplate.opsForValue().set(lockKey, attemptCount, lockSeconds, TimeUnit.SECONDS);
                log.info("账号[{}]登录失败次数达到{}次，锁定{}秒", username, attemptCount, lockSeconds);
            }
        }
        return attemptCount;
    }

    /**
     * 登录成功后清除记录.
     *
     * @param username 用户名
     */
    public void recordSuccess(String username) {
        if (!this.isEnabled()) {
            return;
        }
        String attemptKey = this.buildAttemptKey(username);
        String lockKey = this.buildLockKey(username);
        this.redisTemplate.delete(attemptKey);
        this.redisTemplate.delete(lockKey);
    }

    /**
     * 获取当前失败次数.
     *
     * @param username 用户名
     * @return 失败次数
     */
    public Integer getAttemptCount(String username) {
        String attemptKey = this.buildAttemptKey(username);
        Integer count = (Integer) this.redisTemplate.opsForValue().get(attemptKey);
        return Objects.nonNull(count) ? count : 0;
    }

    /**
     * 校验图形验证码.
     *
     * @param captchaKey 验证码key
     * @param captcha    用户输入的验证码
     * @return true表示验证通过
     */
    public boolean validateCaptcha(String captchaKey, String captcha) {
        if (!this.isEnabled()) {
            return true;
        }
        String key = this.authProperty.getLoginSecurity().getCaptchaKeyPrefix() + captchaKey;
        Object storedCaptcha = this.redisTemplate.opsForValue().get(key);
        if (Objects.isNull(storedCaptcha)) {
            return false;
        }
        return CharSequenceUtil.equalsIgnoreCase(storedCaptcha.toString(), captcha);
    }

    /**
     * 删除已使用的图形验证码.
     *
     * @param captchaKey 验证码key
     */
    public void deleteCaptcha(String captchaKey) {
        if (!this.isEnabled()) {
            return;
        }
        String key = this.authProperty.getLoginSecurity().getCaptchaKeyPrefix() + captchaKey;
        this.redisTemplate.delete(key);
    }

    private boolean isEnabled() {
        LoginSecurityConfig config = this.authProperty.getLoginSecurity();
        return Objects.nonNull(config) && Boolean.TRUE.equals(config.getEnabled());
    }

    private String buildAttemptKey(String username) {
        return this.authProperty.getLoginSecurity().getAttemptKeyPrefix() + username;
    }

    private String buildLockKey(String username) {
        return this.authProperty.getLoginSecurity().getLockKeyPrefix() + username;
    }
}
