/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 日期处理工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class DatesUtil extends DateUtil {

    /**
     * 时间单位映射.
     */
    private static final Map<String, ChronoUnit> TIME_UNIT_MAP = Map.of(
        "s", ChronoUnit.SECONDS,
        "m", ChronoUnit.MINUTES,
        "h", ChronoUnit.HOURS
    );

    /**
     * 毫秒转LocalDateTime.
     *
     * @param millis 毫秒
     * @return LocalDateTime
     */
    public static LocalDateTime parseDate(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    /**
     * millis转String.
     *
     * @param millis 毫秒
     * @param format 格式
     * @return String
     */
    public static String parseDate(long millis, String format) {
        final LocalDateTime localDateTime = DatesUtil.parseDate(millis);
        return DateUtil.format(localDateTime, format);
    }

    /**
     * 将字符串日期转为LocalDateTime.
     *
     * @param stringDate 字符串日期
     * @return LocalDateTime
     */
    public static LocalDateTime parseDate(String stringDate) {
        // 先尝试直接解析为日期时间格式
        String pattern = DatePattern.NORM_DATETIME_PATTERN;
        try {
            return DateUtil.parseLocalDateTime(stringDate, pattern);
        } catch (Exception ex) {
            log.warn("Failed to parse date with pattern: {}, trying with date only", pattern);
        }

        // 尝试解析为日期格式，然后拼接默认时间
        try {
            LocalDate localDate = LocalDate.parse(stringDate, DatePattern.NORM_DATE_FORMATTER);
            return localDate.atStartOfDay();
        } catch (Exception ex) {
            log.error("Failed to parse date: {}", stringDate, ex);
            throw new BizException(BizCodeEnum.PARSE_ERROR, "Invalid date format: " + stringDate);
        }
    }

    /**
     * 将utc字符串日期转为LocalDateTime.
     *
     * @param stringDate 字符串日期
     * @return LocalDateTime
     */
    public static LocalDateTime parseUtcDate(String stringDate) {
        return LocalDateTime.ofInstant(Instant.parse(stringDate), ZoneOffset.UTC.normalized())
            .atZone(ZoneOffset.UTC.normalized())
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    /**
     * 获得当天0点时间.
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getTimesMorning() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    /**
     * 获得当天结束时间.
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getTimesNight() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
    }

    /**
     * 两个日期之间所有的日期.
     *
     * @param startDateString 开始日期
     * @param endDateString   结束日期
     * @return List<String>
     */
    public static List<String> getDaysBetween(String startDateString, String endDateString) {
        List<DateTime> datesBetween = DateUtil.rangeToList(DateUtil.parseDate(startDateString), DateUtil.parseDate(endDateString),
            DateField.DAY_OF_MONTH);
        return datesBetween.stream().map(p -> DateUtil.format(p, DatePattern.NORM_DATE_PATTERN)).toList();
    }

    /**
     * 两个日期之间所有的周.
     *
     * @param startDateString 开始日期
     * @param endDateString   结束日期
     * @return List<String>
     */
    public static List<String> getWeeksBetween(String startDateString, String endDateString) {
        // 这种情况必须重置结束日期，结束日期最好改成这周的最后一天
        endDateString = DateUtil.format(DateUtil.endOfWeek(DateUtil.parse(endDateString)), DatePattern.NORM_DATE_PATTERN);
        List<DateTime> datesBetween = DateUtil.rangeToList(DateUtil.parseDate(startDateString), DateUtil.parseDate(endDateString),
            DateField.WEEK_OF_YEAR);
        return datesBetween.stream().map(p -> buildRangeString(DateUtil.beginOfWeek(p), DateUtil.endOfWeek(p), "~")).toList();
    }
    private static String buildRangeString(DateTime start, DateTime end, String symbol) {
        return DateUtil.format(start, DatePattern.NORM_DATE_PATTERN) + symbol + DateUtil.format(end, DatePattern.NORM_DATE_PATTERN);
    }

    /**
     * 两个日期之间所有的月.
     *
     * @param startDateString 开始日期
     * @param endDateString   结束日期
     * @return List<String>
     */
    public static List<String> getMonthsBetween(String startDateString, String endDateString) {
        // 这种情况必须重置结束日期，结束日期最好改成这月的最后一天
        endDateString = DateUtil.format(DateUtil.endOfMonth(DateUtil.parse(endDateString)), DatePattern.NORM_DATE_PATTERN);
        List<DateTime> datesBetween = DateUtil.rangeToList(DateUtil.parseDate(startDateString), DateUtil.parseDate(endDateString), DateField.MONTH);
        return datesBetween.stream().map(p -> DateUtil.format(p, DatePattern.NORM_MONTH_PATTERN)).toList();
    }

    /**
     * 计算两个时间相差毫秒数.
     *
     * @param beginLocalDateTime 开始时间
     * @param endLocalDateTime   结束时间
     * @return long
     */
    public static long getMillis(LocalDateTime beginLocalDateTime, LocalDateTime endLocalDateTime) {
        return Duration.between(beginLocalDateTime, endLocalDateTime).toMillis();
    }

    /**
     * 判断两个时间间隔时长是否等于给定的时差.
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @param interval  时差（秒）
     * @return boolean
     */
    public static boolean checkLocalTime(LocalTime beginTime, LocalTime endTime, int interval) {
        long durationInSeconds = Duration.between(beginTime, endTime).getSeconds();
        return durationInSeconds == interval;
    }

    /**
     * 判断时间戳是否合理 （当只存在min时，判断最小值，当只存在max时，判断最大值，当min和max都存在时，判断是否在范围内）.
     * </p>
     * min，max:负数代表过去，正数代表未来.
     * </p>
     * 如 dateField=year（min=-3：证件有效期不超过3年），（min=3：保质期至少3年）
     * </p>
     * 如 dateField=year（max=-3：证件有效期至少3年），（max=3：保质期不超过3年）
     * </p>
     * 如 dateField=year（min=-30，max=-10：年龄在10至30岁之间）（min=10，max=30，档案保管期限在10至30年之间）
     * </p>
     *
     * @param timestamp 时间戳
     * @param dateField 时间单位
     * @param min       最小值
     * @param max       最大值
     * @return boolean
     */
    public static boolean checkDateTime(long timestamp, DateField dateField, Integer min, Integer max) {
        if (ObjectUtil.isEmpty(min) && ObjectUtil.isEmpty(max)) {
            throw new BizException(BizCodeEnum.MIN_MAX_EMPTY);
        }
        final DateTime dateTime = DateUtil.date();
        if (ObjectUtil.isNotEmpty(min) && ObjectUtil.isEmpty(max)) {
            DateTime minDateTime = DatesUtil.offset(DatesUtil.beginOfDay(dateTime), dateField, min);
            return timestamp >= minDateTime.getTime();
        } else if (ObjectUtil.isEmpty(min) && ObjectUtil.isNotEmpty(max)) {
            DateTime maxDateTime = DatesUtil.offset(DatesUtil.endOfDay(dateTime), dateField, max);
            return timestamp <= maxDateTime.getTime();
        } else {
            DatesUtil.checkMinMaxLegal(min, max);
            DateTime minDateTime = DatesUtil.offset(DatesUtil.beginOfDay(dateTime), dateField, min);
            DateTime maxDateTime = DatesUtil.offset(DatesUtil.endOfDay(dateTime), dateField, max);
            return timestamp >= minDateTime.getTime() && timestamp <= maxDateTime.getTime();
        }
    }

    /**
     * 将时间频率字符串转换为LocalDateTime。
     *
     * @param baseTime  基准时间
     * @param frequency 时间频率字符串，例如 "5s,10s,20s,30s,1m,30m,1h"
     * @return List<LocalDateTime>
     */
    public static List<LocalDateTime> convertFrequencyToLocalDateTime(LocalDateTime baseTime, String frequency) {
        if (CharSequenceUtil.isEmpty(frequency)) {
            return Collections.emptyList();
        }

        List<String> frequencies = CharSequenceUtil.splitTrim(frequency, ',');
        List<LocalDateTime> result = new ArrayList<>(10);
        for (String freq : frequencies) {
            final String digitStr = freq.replaceAll("[^\\d]", "");
            if(CharSequenceUtil.isBlank(digitStr)) {
                throw new IllegalArgumentException("Unsupported frequency format: " + freq);
            }
            long amount = Long.parseLong(digitStr);
            String unit = freq.replaceAll("[\\d]", "");
            ChronoUnit chronoUnit = TIME_UNIT_MAP.get(unit);
            if (chronoUnit == null) {
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
            }
            result.add(baseTime.plus(amount, chronoUnit));
        }

        return result;
    }

    private static void checkMinMaxLegal(Integer min, Integer max) {
        if (ObjectUtil.isNotEmpty(min) && ObjectUtil.isNotEmpty(max)) {
            boolean bothPositive = min > 0 && max > 0;
            boolean bothNegative = min < 0 && max < 0;

            if (!(bothPositive || bothNegative)) {
                throw new BizException(BizCodeEnum.MIN_MAX_INVALID);
            }
            if (Math.abs(min) > Math.abs(max)) {
                throw new BizException(BizCodeEnum.MIN_MAX_INVALID);
            }
        }
    }
}
