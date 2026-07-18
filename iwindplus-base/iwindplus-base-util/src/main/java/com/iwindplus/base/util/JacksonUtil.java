/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.support.PageSerializer;
import com.iwindplus.base.util.support.SensitiveAnnotationIntrospect;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * jackson工具类.
 *
 * @author zengdegui
 * @since 2024/11/22
 */
@Slf4j
public class JacksonUtil {

    private JacksonUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    private static final Boolean DEFAULT_SENSITIVE_ENABLED =
        Boolean.getBoolean("jackson.sensitive.enabled");
    private static final Boolean DEFAULT_MYBATIS_PAGE_ENABLED =
        Boolean.getBoolean("jackson.mybatis-page.enabled");

    /**
     * 单例 ObjectMapper（线程安全）
     */
    private static volatile ObjectMapper OBJECT_MAPPER = createDefaultObject();

    /**
     * 获取单例 ObjectMapper.
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 设置全局 ObjectMapper.
     *
     * @param objectMapper ObjectMapper
     */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        if (Objects.isNull(objectMapper)) {
            return;
        }

        OBJECT_MAPPER = objectMapper;

        log.info("JacksonUtil 全局 ObjectMapper 已更新");
    }

    /**
     * 递归清理 JsonNode 中的字符串值。
     *
     * @param node     当前需要清理的 JsonNode
     * @param function 函数
     */
    public static void cleanJsonNode(JsonNode node, Function<String, String> function) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.properties().forEach(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isTextual()) {
                    objectNode.put(key, function.apply(value.asText()));
                } else {
                    cleanJsonNode(value, function);
                }
            });
        } else if (node.isArray()) {
            node.elements().forEachRemaining(m -> cleanJsonNode(m, function));
        }
    }

    /**
     * 使用 Jsoup 清理字符串，并返回清理后的结果.
     *
     * @param value     待清理的字符串
     * @param whitelist 白名单列表
     * @return String
     */
    public static String cleanByJsoup(String value, List<String> whitelist) {
        Safelist safelist = Safelist.relaxed();
        if (CollUtil.isNotEmpty(whitelist)) {
            safelist.addTags(whitelist.toArray(String[]::new));
        }

        try {
            return CharSequenceUtil.trim(Jsoup.clean(value, safelist));
        } catch (Exception ex) {
            log.error("Error cleaning value with Jsoup", ex);
            return value;
        }
    }

    /**
     * 通用的 ObjectMapper 执行方法，执行指定操作。
     *
     * @param action 执行的操作
     * @param <T>    返回的类型
     * @return 执行结果
     */
    public static <T> T executeAction(ObjectMapperAction<T> action) {
        try {
            return action.execute(getMapper());
        } catch (Exception ex) {
            log.error("ObjectMapper action execution error", ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        }
    }

    /**
     * 将对象转换为字节数组.
     *
     * @param obj 对象
     * @return byte[]
     */
    public static byte[] toJsonBytes(Object obj) {
        if (null == obj) {
            return new byte[0];
        }
        return executeAction(mapper -> mapper.writeValueAsBytes(obj));
    }

    /**
     * 将对象转换为 JSON 字符串.
     *
     * @param obj 对象
     * @return String
     */
    public static String toJsonStr(Object obj) {
        if (null == obj) {
            return null;
        }
        return executeAction(mapper -> mapper.writeValueAsString(obj));
    }

    /**
     * 格式化输出 JSON 字符串（带缩进）.
     *
     * @param obj 对象
     * @return String
     */
    public static String toJsonPrettyStr(Object obj) {
        if (null == obj) {
            return null;
        }
        return executeAction(mapper ->
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
        );
    }

    /**
     * 将对象转换为字节数组（格式化）.
     *
     * @param obj 对象
     * @return byte[]
     */
    public static byte[] toJsonPrettyBytes(Object obj) {
        if (null == obj) {
            return new byte[0];
        }
        return executeAction(mapper ->
            mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(obj)
        );
    }

    /**
     * 将将对象转换为任意目标类型.
     *
     * @param obj 源对象
     * @param <T> 泛型
     * @return T
     */
    public static <T> T convertValue(Object obj, Class<T> clazz) {
        if (null == obj || null == clazz) {
            return null;
        }
        return executeAction(mapper -> mapper.convertValue(obj, clazz));
    }

    /**
     * 将 字节数组转换为对象.
     *
     * @param bytes 字节
     * @param clazz 类
     * @param <T>   泛型
     * @return T
     */
    public static <T> T parseBytes(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes) || null == clazz) {
            return null;
        }
        return executeAction(mapper -> mapper.readValue(bytes, clazz));
    }

    /**
     * 将 JSON 字符串转换为对象.
     *
     * @param text  JSON 字符串
     * @param clazz 目标类
     * @param <T>   泛型
     * @return T
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (CharSequenceUtil.isBlank(text) || null == clazz) {
            return null;
        }

        return executeAction(mapper -> mapper.readValue(text, clazz));
    }

    /**
     * 将 JSON 字符串转换为对象.
     *
     * @param text    JSON 字符串
     * @param typeRef 目标类型
     * @param <T>     泛型
     * @return T
     */
    public static <T> T parseObject(String text, ParameterizedTypeReference<T> typeRef) {
        if (CharSequenceUtil.isBlank(text) || null == typeRef) {
            return null;
        }

        return executeAction(entity -> {
            TypeReference<T> jacksonTypeRef = new TypeReference<>() {
                @Override
                public Type getType() {
                    return typeRef.getType();
                }
            };
            return entity.readValue(text, jacksonTypeRef);
        });
    }

    /**
     * 将 输入流转换为对象.
     *
     * @param inputStream 输入流
     * @param clazz       目标类
     * @param <T>         泛型
     * @return T
     */
    public static <T> T parseObject(InputStream inputStream, Class<T> clazz) {
        if (Objects.isNull(inputStream) || null == clazz) {
            return null;
        }

        return executeAction(mapper -> mapper.readValue(inputStream, clazz));
    }

    /**
     * 将Map转换为对象.
     *
     * @param map   map
     * @param clazz 目标类
     * @param <T>   泛型
     * @return T
     */
    public static <T> T parseObject(Map<String, Object> map, Class<T> clazz) {
        if (MapUtil.isEmpty(map) || null == clazz) {
            return null;
        }

        return executeAction(mapper -> mapper.convertValue(map, clazz));
    }

    /**
     * 使用 TypeReference 解析 JSON 字符串.
     *
     * @param text          JSON 字符串
     * @param typeReference 类型引用
     * @param <T>           泛型
     * @return T
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (CharSequenceUtil.isBlank(text) || null == typeReference) {
            return null;
        }

        return executeAction(mapper -> mapper.readValue(text, typeReference));
    }

    /**
     * 使用 javaType 解析 JSON 字符串.
     *
     * @param text     JSON 字符串
     * @param javaType java类型
     * @param <T>      泛型
     * @return T
     */
    public static <T> T parseObject(String text, JavaType javaType) {
        if (CharSequenceUtil.isBlank(text) || null == javaType) {
            return null;
        }

        return executeAction(mapper -> mapper.readValue(text, javaType));
    }

    /**
     * 将 JSON 字符串转换为Map对象.
     *
     * @param text JSON 字符串
     * @return Map<String, Object>
     */
    public static Map<String, Object> parseMap(String text) {
        return parseObject(text, new TypeReference<>() {
        });
    }

    /**
     * 解析 JSON 字符串为 List<Object>.
     *
     * @param text  JSON 字符串
     * @param clazz 泛型类
     * @param <T>   泛型
     * @return List<T>
     */
    public static <T> List<T> parseList(String text, Class<T> clazz) {
        if (CharSequenceUtil.isBlank(text) || null == clazz) {
            return Collections.emptyList();
        }

        return executeAction(entity -> {
            TypeFactory tf = entity.getTypeFactory();
            return entity.readValue(text, tf.constructCollectionType(List.class, clazz));
        });
    }

    /**
     * 解析 JSON 字符串为 Set<Object>.
     *
     * @param text  JSON 字符串
     * @param clazz 泛型类
     * @param <T>   泛型
     * @return Set<T>
     */
    public static <T> Set<T> parseSet(String text, Class<T> clazz) {
        if (CharSequenceUtil.isBlank(text) || null == clazz) {
            return null;
        }

        return executeAction(entity -> {
            TypeFactory tf = entity.getTypeFactory();
            return entity.readValue(text, tf.constructCollectionType(Set.class, clazz));
        });
    }

    /**
     * 解析 JSON 字符串为 JsonNode.
     *
     * @param text JSON 字符串
     * @return JsonNode
     */
    public static JsonNode parseTree(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            return null;
        }

        return executeAction(mapper -> mapper.readTree(text));
    }

    /**
     * 创建默认 ObjectMapper.
     *
     * @return ObjectMapper
     */
    public static ObjectMapper createDefaultObject() {
        JacksonProperties properties = new JacksonProperties();
        Jackson2ObjectMapperBuilderCustomizer customizer =
            jackson2ObjectMapperBuilderCustomizer(
                properties,
                DEFAULT_SENSITIVE_ENABLED,
                DEFAULT_MYBATIS_PAGE_ENABLED
            );
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        customizer.customize(builder);
        return builder.build();
    }

    /**
     * 创建 Jackson2ObjectMapperBuilderCustomizer.
     *
     * @param jacksonProperties  jackson配置
     * @param sensitiveEnabled   是否开启敏感信息脱敏
     * @param mybatisPageEnabled 是否开启mybatis自定义分页响应
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    public static Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(
        JacksonProperties jacksonProperties,
        boolean sensitiveEnabled,
        boolean mybatisPageEnabled) {
        return builder -> {
            String dateFormat = Optional.ofNullable(jacksonProperties)
                .map(JacksonProperties::getDateFormat)
                .orElse(DatePattern.NORM_DATETIME_PATTERN);
            Locale locale = Optional.ofNullable(jacksonProperties)
                .map(JacksonProperties::getLocale)
                .orElse(Locale.getDefault());
            TimeZone timeZone = Optional.ofNullable(jacksonProperties)
                .map(JacksonProperties::getTimeZone)
                .orElse(TimeZone.getDefault());

            builder.simpleDateFormat(dateFormat)
                .locale(locale)
                .timeZone(timeZone);

            if (sensitiveEnabled) {
                builder.annotationIntrospector(new SensitiveAnnotationIntrospect(true));
            }

            final List<Module> modules = new ArrayList<>(4);
            modules.add(buildTimeModule(dateFormat));
            if (mybatisPageEnabled) {
                modules.add(buildMybatisPageModule(true));
            }
            builder.modules(modules);

            builder.serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .featuresToEnable(
                    SerializationFeature.WRITE_ENUMS_USING_TO_STRING,
                    JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN,
                    SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS,
                    SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
                    DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                    DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                    DeserializationFeature.READ_ENUMS_USING_TO_STRING,
                    JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS.mappedFeature()
                )
                .featuresToDisable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                    SerializationFeature.FAIL_ON_EMPTY_BEANS,
                    DeserializationFeature.ACCEPT_FLOAT_AS_INT,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                );
        };
    }

    private static Module buildTimeModule(String dateFormat) {
        JavaTimeModule timeModule = new JavaTimeModule();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN);
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern(dateFormat);

        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFmt));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFmt));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFmt));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFmt));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFmt));
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFmt));
        timeModule.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        timeModule.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        return timeModule;
    }

    private static Module buildMybatisPageModule(boolean globalEnabled) {
        SimpleModule pageModule = new SimpleModule("mybatisPage");
        pageModule.addSerializer(Page.class, new PageSerializer(globalEnabled));
        return pageModule;
    }

    @FunctionalInterface
    private interface ObjectMapperAction<T> {

        /**
         * 执行.
         *
         * @param entity ObjectMapper
         * @return T
         * @throws Exception
         */
        T execute(ObjectMapper entity) throws Exception;
    }
}
