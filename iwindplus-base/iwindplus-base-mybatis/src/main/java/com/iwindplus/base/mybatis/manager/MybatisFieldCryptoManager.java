package com.iwindplus.base.mybatis.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.mybatis.domain.DbSignBaseDO;
import com.iwindplus.base.domain.annotation.TableFieldSafe;
import com.iwindplus.base.domain.annotation.TableFieldSensitive;
import com.iwindplus.base.domain.enums.AlgorithmTypeEnum;
import com.iwindplus.base.mybatis.domain.property.MybatisProperty;
import com.iwindplus.base.mybatis.domain.property.MybatisProperty.FieldConfig.CryptoConfig;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.DbSignUtil;
import com.iwindplus.base.util.DbUtil;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.util.SensitiveUtil;
import com.iwindplus.base.util.domain.dto.DbSignGenerateDTO;
import com.iwindplus.base.util.domain.dto.SensitiveDTO;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * MyBatis 字段级 加解密 & 脱敏 管理器（零反射运行期版） 支持 JDK 8/11/17+ 模块化，支持非 String 字段 JSON 序列化。 线程安全，可随配置热更新。
 *
 * @author zengdegui
 * @since 2025-04-22
 */
@Slf4j
@Getter
public class MybatisFieldCryptoManager {

    private static final Cache<Class<?>, EntityHolder> HOLDER_CACHE =
        Caffeine.newBuilder()
            .maximumSize(4096)
            .build();

    private static final Cache<String, SensitiveDTO> SENSITIVE_CACHE =
        Caffeine.newBuilder()
            .maximumSize(512)
            .build();

    /**
     * 线程安全的 Lookup 缓存，避免重复创建 Lookup
     */
    private static final Cache<Class<?>, MethodHandles.Lookup> LOOKUP_CACHE =
        Caffeine.newBuilder()
            .maximumSize(256)
            .build();

    private final Map<String, String> dbNameCache = new ConcurrentHashMap<>(16);
    private final Map<Class<?>, String> tableNameCache = new ConcurrentHashMap<>(16);
    private final Map<AlgorithmTypeEnum, CryptoConfig> cryptoConfigCache = new ConcurrentHashMap<>(16);

    private final MybatisPlusProperties mybatisPlusProperties;
    private final MybatisProperty mybatisProperty;


    public MybatisFieldCryptoManager(MybatisPlusProperties mybatisPlusProperties,
        MybatisProperty mybatisProperty) {
        this.mybatisPlusProperties = mybatisPlusProperties;
        this.mybatisProperty = mybatisProperty;
        scanClassesField(mybatisPlusProperties);
        log.info("MybatisFieldManager 加载完成");
    }

    /**
     * 入库加密.
     *
     * @param source 数据源
     */
    public void encryptOnInput(Object source) {
        processHandler(source, true, false, false);
    }

    /**
     * 出库解密.
     *
     * @param source 数据源
     */
    public void decryptOnOutput(Object source) {
        processHandler(source, false, false, true);
    }

    /**
     * 入库脱敏.
     *
     * @param source 数据源
     */
    public void sensitiveOnInput(Object source) {
        processHandler(source, false, true, false);
    }

    /**
     * 出库脱敏.
     *
     * @param source 数据源
     */
    public void sensitiveOnOutput(Object source) {
        processHandler(source, false, true, false);
    }

    /**
     * 清空缓存.
     */
    public void clearCache() {
        HOLDER_CACHE.invalidateAll();
        SENSITIVE_CACHE.invalidateAll();
        LOOKUP_CACHE.invalidateAll();
        cryptoConfigCache.clear();

        log.info("MybatisFieldManager 缓存已清空");
    }

    /**
     * 设置签名.
     *
     * @param ms        ms
     * @param param     参数
     * @param secretKey 加签密钥
     * @param cmd       操作
     */
    public void addSign(MappedStatement ms, Object param, String secretKey, SqlCommandType cmd) {
        if (CharSequenceUtil.isBlank(secretKey)) {
            return;
        }

        final List<Object> list = this.parseObj(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 判断是否时需签名
        boolean hasSignEntity = list.stream().anyMatch(DbSignBaseDO.class::isInstance);
        if (!hasSignEntity) {
            return;
        }

        // 去重：使用 IdentityHashMap 确保同一对象只处理一次
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        list.removeIf(obj -> !seen.add(obj));

        // 获取db
        final String dbName = getDbName(ms);
        if (CharSequenceUtil.isBlank(dbName)) {
            return;
        }

        // 获取table
        DbSignBaseDO entity = list.stream()
            .filter(DbSignBaseDO.class::isInstance)
            .map(DbSignBaseDO.class::cast)
            .findFirst()
            .orElse(null);
        if (Objects.isNull(entity)) {
            return;
        }
        final String tableName = getTableName(entity.getClass());
        if (CharSequenceUtil.isBlank(tableName)) {
            return;
        }

        list.stream()
            .filter(DbSignBaseDO.class::isInstance)
            .map(DbSignBaseDO.class::cast)
            .forEach(obj -> this.doSign(dbName, tableName, obj, secretKey, cmd));
    }

    /**
     * 解析对象.
     *
     * @param source 数据源
     * @return List
     */
    public List<Object> parseObj(Object source) {
        if (source == null) {
            return Collections.emptyList();
        }

        // 每次方法调用独立创建（无共享风险）
        ArrayDeque<Object> stack = new ArrayDeque<>(16);
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Object> result = new ArrayList<>(16);

        stack.push(source);

        while (!stack.isEmpty()) {
            Object obj = stack.pop();

            if (obj == null) {
                continue;
            }
            if (!visited.add(obj)) {
                continue;
            }

            Class<?> clazz = obj.getClass();

            if (obj instanceof Map<?, ?> map) {
                for (Object v : map.values()) {
                    if (v != null) {
                        stack.push(v);
                    }
                }
            } else if (obj instanceof Collection<?> col) {
                for (Object v : col) {
                    if (v != null) {
                        stack.push(v);
                    }
                }
            } else if (clazz.isArray()) {
                Object[] arr = (Object[]) obj;
                for (Object v : arr) {
                    if (v != null) {
                        stack.push(v);
                    }
                }
            } else {
                result.add(obj);
            }
        }

        return result;
    }

    /**
     * 获取数据库名.
     *
     * @param ms ms
     * @return String
     */
    public String getDbName(MappedStatement ms) {
        DataSource ds = ms.getConfiguration().getEnvironment().getDataSource();
        String dsKey = ds.getClass().getName() + "@" + System.identityHashCode(ds);

        return dbNameCache.computeIfAbsent(dsKey, k -> {
            Connection connection = null;
            try {
                connection = DataSourceUtils.getConnection(ds);
                if (connection == null) {
                    log.warn("Failed to get connection from dataSource: {}", ds);
                    return null;
                }

                return DbUtil.getRealDbName(connection);
            } finally {
                DataSourceUtils.releaseConnection(connection, ds);
            }
        });
    }

    /**
     * 获取表名.
     *
     * @param clazz 类
     * @return 表名
     */
    public String getTableName(Class<?> clazz) {
        return tableNameCache.computeIfAbsent(clazz, k -> {
            TableInfo info = TableInfoHelper.getTableInfo(k);
            if (info == null) {
                return null;
            }
            return info.getTableName()
                .replace("`", "")
                .replace("\"", "");
        });
    }

    /**
     * 处理助手.
     *
     * @param source         原对象
     * @param inputEncrypt   是否启用输入加密
     * @param inputSensitive 是否启用输入脱敏
     * @param outputDecrypt  是否启用输出解密
     */
    public void processHandler(Object source, boolean inputEncrypt, boolean inputSensitive, boolean outputDecrypt) {
        if (source == null) {
            return;
        }

        // 防空串
        if (source instanceof CharSequence cs && CharSequenceUtil.isBlank(cs)) {
            return;
        }

        final List<Object> list = parseObj(source);
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 单条记录直接处理，避免异步开销
        if (list.size() == 1) {
            processSingle(list.get(0), inputEncrypt, inputSensitive, outputDecrypt);
            return;
        }

        for (Object o : list) {
            processSingle(o, inputEncrypt, inputSensitive, outputDecrypt);
        }
    }

    /**
     * 处理单个对象.
     */
    private void processSingle(Object o, boolean enc, boolean sen, boolean dec) {
        if (o == null) {
            return;
        }

        EntityHolder holder = HOLDER_CACHE.get(o.getClass(), this::buildHolder);
        if (holder == null) {
            return;
        }

        try {
            if (enc && holder.hasEncrypt()) {
                apply(o, holder.getEncrypt(), this::doEncrypt);
            }
            if (dec && holder.hasDecrypt()) {
                apply(o, holder.getDecrypt(), this::doDecrypt);
            }
            if (sen && holder.hasSensitive()) {
                apply(o, holder.getSensitive(), this::doSensitive);
            }
        } catch (Throwable ex) {
            log.warn("process error {}", o.getClass().getSimpleName(), ex);
        }
    }

    /**
     * 字段执行器.
     */
    private void apply(Object o, FieldHandle[] handles, BiFunction<Object, FieldMetaDTO, Object> operator) {
        for (FieldHandle h : handles) {
            try {
                Object oldVal = h.getGetter().invoke(o);
                if (oldVal == null) {
                    continue;
                }
                Object newVal = operator.apply(oldVal, h.getMeta());
                if (newVal != null && !newVal.equals(oldVal)) {
                    h.getSetter().invoke(o, newVal);
                }
            } catch (Throwable t) {
                log.warn("Apply field {} failed: {}", h.getField().getName(), t.getMessage());
            }
        }
    }

    /**
     * 加密处理.
     */
    private Object doEncrypt(Object val, FieldMetaDTO m) {
        // null 直接返回
        if (val == null) {
            return null;
        }

        String str;
        // String 直接用
        if (val instanceof String s) {
            if (CharSequenceUtil.isBlank(s)) {
                return s;
            }

            str = s;
        } else {
            // 非 String 统一 JSON 化
            str = JacksonUtil.toJsonStr(val);
        }

        if (CharSequenceUtil.isBlank(str)) {
            return str;
        }

        // 执行加密
        return CryptoUtil.encrypt(str, cryptoConfig(m.getAlgorithm()));
    }

    /**
     * 解密处理.
     */
    private Object doDecrypt(Object val, FieldMetaDTO m) {
        if (!(val instanceof String str)) {
            return val;
        }

        // 空串直接跳过
        if (CharSequenceUtil.isBlank(str)) {
            return str;
        }

        // 非密文直接跳过
        if (!isCipherText(str)) {
            return str;
        }

        String dec = CryptoUtil.decrypt(str, cryptoConfig(m.getAlgorithm()));

        return m.getField().getType() == String.class
            ? dec
            : JacksonUtil.parseObject(dec, m.getField().getType());
    }

    /**
     * 脱敏处理.
     */
    private Object doSensitive(Object val, FieldMetaDTO m) {
        if (val == null) {
            return null;
        }

        // 如果是 String
        if (val instanceof String str) {
            if (CharSequenceUtil.isBlank(str)) {
                return str;
            }

            // 防重复脱敏（关键）
            if (isAlreadySensitive(str)) {
                return str;
            }

            SensitiveDTO dto = buildSensitiveDTO(m.getSensitiveAnn());
            if (dto == null) {
                return str;
            }

            return SensitiveUtil.desensitized(str, dto);
        }

        // 非 String → JSON（慎用）
        SensitiveDTO dto = buildSensitiveDTO(m.getSensitiveAnn());
        if (dto == null) {
            return val;
        }

        String json = JacksonUtil.toJsonStr(val);

        // JSON 层脱敏风险提示（建议生产避免）
        String result = SensitiveUtil.desensitized(json, dto);

        return JacksonUtil.parseObject(result, val.getClass());
    }

    private boolean isAlreadySensitive(String str) {
        // 常见手机号脱敏特征
        if (str.contains("*")) {
            return true;
        }

        // 可以根据规则扩展：
        // 邮箱 / 手机 / 身份证
        return false;
    }

    private boolean isCipherText(String str) {
        // 空直接 false
        if (CharSequenceUtil.isBlank(str)) {
            return false;
        }

        // 长度兜底（AEAD至少有tag）
        if (str.length() < 32) {
            return false;
        }

        // base64 特征（按你 CryptoUtil 调整）
        return str.matches("^[A-Za-z0-9+/=]+$");
    }

    /**
     * 扫描类字段.
     */
    private void scanClassesField(MybatisPlusProperties props) {
        String pkg = props.getTypeAliasesPackage();
        Class<?> superType = props.getTypeAliasesSuperType();
        Set<Class<?>> classes = FilesUtil.scanClasses(pkg, superType);
        if (CollUtil.isEmpty(classes)) {
            return;
        }

        for (Class<?> clz : classes) {
            HOLDER_CACHE.get(clz, this::buildHolder);
        }
    }

    /**
     * 构建实体持有者.
     */
    private EntityHolder buildHolder(Class<?> clz) {
        List<Field> all = FieldUtils.getAllFieldsList(clz);

        List<FieldHandle> encrypt = new ArrayList<>(10);
        List<FieldHandle> decrypt = new ArrayList<>(10);
        List<FieldHandle> sensitive = new ArrayList<>(10);
        for (Field field : all) {
            FieldHandle handle = buildFieldHandle(field, clz);
            if (handle == null) {
                continue;
            }

            if (handle.getMeta().needEncrypt()) {
                encrypt.add(handle);
            }

            if (handle.getMeta().needDecrypt()) {
                decrypt.add(handle);
            }

            if (handle.getMeta().needDesensitize()) {
                sensitive.add(handle);
            }
        }

        if (encrypt.isEmpty() && decrypt.isEmpty() && sensitive.isEmpty()) {
            return null;
        }

        return new EntityHolder(
            encrypt.toArray(new FieldHandle[0]),
            decrypt.toArray(new FieldHandle[0]),
            sensitive.toArray(new FieldHandle[0])
        );
    }

    /**
     * 构建字段处理器.
     */
    private FieldHandle buildFieldHandle(Field field, Class<?> clazz) {
        FieldMetaDTO meta = buildMeta(field);
        if (meta == null) {
            return null;
        }

        try {
            MethodHandles.Lookup lookup =
                LOOKUP_CACHE.get(clazz, k -> {
                    try {
                        return MethodHandles.privateLookupIn(k, MethodHandles.lookup());
                    } catch (Throwable e) {
                        return MethodHandles.lookup();
                    }
                });

            MethodHandle getter = lookup.unreflectGetter(field);
            MethodHandle setter = lookup.unreflectSetter(field);

            return new FieldHandle(
                meta,
                getter,
                setter,
                field
            );
        } catch (Throwable e) {
            log.warn(
                "build field handle fail {}#{}",
                clazz.getSimpleName(),
                field.getName(),
                e
            );
            return null;
        }
    }

    /**
     * 构建字段元数据.
     */
    private FieldMetaDTO buildMeta(Field f) {
        TableFieldSafe safe = f.getAnnotation(TableFieldSafe.class);
        TableFieldSensitive sen = f.getAnnotation(TableFieldSensitive.class);
        // 卫语句，避免写反
        if (safe == null && sen == null) {
            return null;
        }
        AlgorithmTypeEnum alg = safe == null ? null : safe.algorithm();
        return new FieldMetaDTO(f, alg, safe, sen);
    }

    /**
     * 获取加密配置.
     */
    private CryptoConfig cryptoConfig(AlgorithmTypeEnum alg) {
        return cryptoConfigCache.computeIfAbsent(alg, k -> {
            CryptoConfig origin = mybatisProperty.getField().getCrypto();
            return CryptoConfig.builder()
                .enabled(origin.getEnabled())
                .enabledInputEncrypt(origin.getEnabledInputEncrypt())
                .enabledInputSensitive(origin.getEnabledInputSensitive())
                .enabledOutputDecrypt(origin.getEnabledOutputDecrypt())
                .enabledSign(origin.getEnabledSign())
                .publicKey(origin.getPublicKey())
                .privateKey(origin.getPrivateKey())
                .key(origin.getKey())
                .secretKey(origin.getSecretKey())
                .algorithm(alg)
                .build();
        });
    }

    /**
     * 构建脱敏 DTO.
     */
    private SensitiveDTO buildSensitiveDTO(TableFieldSensitive ann) {
        if (ann == null) {
            return null;
        }
        String key = ann.type() + "_" + ann.startInclude() + "_" + ann.endReserve();
        return SENSITIVE_CACHE.get(key, k ->
            SensitiveDTO.builder()
                .type(ann.type())
                .startInclude(ann.startInclude())
                .endReserve(ann.endReserve())
                .build());
    }

    /**
     * 执行签名.
     */
    private void doSign(String dbName, String tableName, DbSignBaseDO entity, String secretKey, SqlCommandType cmd) {
        String action = cmd.name();

        long salt = IdWorker.getId();
        entity.setSalt(salt);

        DbSignGenerateDTO dto = DbSignGenerateDTO.builder()
            .secretKey(secretKey)
            .salt(salt)
            .dbName(dbName)
            .tableName(tableName)
            .action(action)
            .build();
        String sign = DbSignUtil.generateSign(dto);
        entity.setSign(sign);
    }

    /**
     * 字段处理器.
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    private static final class FieldHandle {

        private final FieldMetaDTO meta;
        private final MethodHandle getter;
        private final MethodHandle setter;
        private final Field field;
    }

    /**
     * 实体持有者.
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    private static final class EntityHolder {

        private final FieldHandle[] encrypt;
        private final FieldHandle[] decrypt;
        private final FieldHandle[] sensitive;

        /**
         * 是否有加密字段.
         */
        boolean hasEncrypt() {
            return encrypt != null && encrypt.length > 0;
        }

        /**
         * 是否有解密字段.
         */
        boolean hasDecrypt() {
            return decrypt != null && decrypt.length > 0;
        }

        /**
         * 是否有脱敏字段.
         */
        boolean hasSensitive() {
            return sensitive != null && sensitive.length > 0;
        }
    }

    /**
     * 字段元数据 DTO.
     */
    @Data
    @SuperBuilder
    @AllArgsConstructor
    private static class FieldMetaDTO {

        private final Field field;
        private final AlgorithmTypeEnum algorithm;
        private final TableFieldSafe safeAnn;
        private final TableFieldSensitive sensitiveAnn;

        /**
         * 是否需要加密.
         *
         * @return boolean
         */
        boolean needEncrypt() {
            return safeAnn != null && safeAnn.inputEncrypt();
        }

        /**
         * 是否需要解密.
         *
         * @return boolean
         */
        boolean needDecrypt() {
            return safeAnn != null && safeAnn.outputDecrypt();
        }

        /**
         * 是否需要脱敏.
         *
         * @return boolean
         */
        boolean needDesensitize() {
            return sensitiveAnn != null && sensitiveAnn.enabled();
        }
    }

}