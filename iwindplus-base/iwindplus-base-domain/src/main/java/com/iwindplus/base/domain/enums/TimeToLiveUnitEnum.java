/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 时间单位枚举.
 *
 * @author zengdegui
 * @since 2024/12/30 21:57
 */
@Getter
@RequiredArgsConstructor
public enum TimeToLiveUnitEnum implements BaseEnum<String> {

    /**
     * 表示纳秒的概念，支持的最小时间单位。对于IS0日历系统，它等于第二单位的千百万分之一
     */
    NANOS("nanos", ChronoUnit.NANOS, "纳秒"),

    /**
     * 表示微秒概念的单位。在IS0日历系统中，它等于第二单位的百万分之一。
     */
    MICROS("micros", ChronoUnit.MICROS, "微秒"),

    /**
     * 表示毫秒概念的单位。对于ISO日历系统，它等于秒单位的千分之一。
     */
    MILLIS("millis", ChronoUnit.MILLIS, "毫秒"),

    /**
     * 表示秒概念的单位。对于ISO历法系统，它等于国际单位制中的秒，除了大约一个闰秒。
     */
    SECONDS("seconds", ChronoUnit.SECONDS, "秒"),

    /**
     * 表示分概念的单位。对于ISO日历系统，它等于60秒。
     */
    MINUTES("minutes", ChronoUnit.MINUTES, "分钟"),

    /**
     * 表示小时概念的单位。对于ISO日历系统，等于60分钟。
     */
    HOURS("hours", ChronoUnit.HOURS, "小时"),

    /**
     * 表示半天概念的单位，如在AM/ PM中使用。对于ISO日历系统，它等于12小时。
     */
    HALF_DAYS("halfDays", ChronoUnit.HALF_DAYS, "半天"),

    /**
     * 表示一天概念的单位。对于ISO日历系统，它是从午夜到午夜的标准一天。一天的估计时长为24小时。 当与其他历法系统一起使用时，它必须与地球上太阳升起和落下所定义的日子相对应。不要求一天从午夜开始-在日历系统之间转换时，日期应该在正午相等。
     */
    DAYS("days", ChronoUnit.DAYS, "天"),

    /**
     * 表示一周概念的单位。对于ISO历法系统，等于7天。 当与其他日历系统一起使用时，它必须对应于整数天数。
     */
    WEEKS("weeks", ChronoUnit.WEEKS, "周"),

    /**
     * 表示月概念的单位。对于ISO日历系统，月的长度随月份而变化。一个月的估计长度是365.2425天的十二分之一。 当与其他日历系统一起使用时，它必须对应于整数天数。
     */
    MONTHS("months", ChronoUnit.MONTHS, "月"),

    /**
     * 表示一年概念的单位。对于ISO日历系统，它等于12个月。一年的估计长度为365.2425天。 当与其他历法系统一起使用时，它必须对应于天数或月份的整数，大致等于由地球绕太阳运行所定义的一年。
     */
    YEARS("years", ChronoUnit.YEARS, "年"),

    /**
     * 代表十年概念的单位。对于ISO历法系统，它等于10年。 当与其他日历系统一起使用时，它必须对应于整数天数，通常是整数年数。
     */
    DECADES("decades", ChronoUnit.DECADES, "十年"),

    /**
     * 代表一个世纪概念的单位。对于ISO历法系统，它等于100年。 当与其他日历系统一起使用时，它必须对应于整数天数，通常是整数年数。
     */
    CENTURIES("centuries", ChronoUnit.CENTURIES, "世纪"),

    /**
     * 代表千禧年概念的单位。对于ISO历法系统，它等于1000年。 当与其他日历系统一起使用时，它必须对应于整数天数，通常是整数年数。
     */
    MILLENNIA("millennia", ChronoUnit.MILLENNIA, "千禧年"),

    /**
     * 代表一个时代概念的单位。对于ISO历法系统，它等于1000000000年.
     */
    ERAS("eras", ChronoUnit.ERAS, "时代"),

    /**
     * 代表永恒概念的人工单位。这主要与TemporalField一起使用，以表示年份或年代等无界字段。此单元的估计持续时间被人为定义为duration支持的最大持续时间。
     */
    FOREVER("forever", ChronoUnit.FOREVER, "永恒");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 时间单位名称对应的实例.
     */
    private final ChronoUnit unit;

    /**
     * 描述.
     */
    private final String desc;

}
