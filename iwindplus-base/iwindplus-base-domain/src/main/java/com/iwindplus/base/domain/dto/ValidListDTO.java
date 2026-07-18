/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import com.iwindplus.base.domain.validation.OtherQueryGroup;
import com.iwindplus.base.domain.validation.OtherSaveGroup;
import com.iwindplus.base.domain.validation.QueryGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义验证List数据传输对象.
 *
 * @param <E> 元素类型
 * @author zengdegui
 * @since 2021/7/8
 */
@Slf4j
@Schema(description = "自定义验证List数据传输对象")
@Data
@SuperBuilder
public class ValidListDTO<E> implements List<E> {

    private static final Map<Class<?>, Field> ID_FIELD_CACHE = new ConcurrentHashMap<>(16);

    /**
     * 对象集合.
     */
    @Schema(description = "对象集合")
    @Valid
    @NotEmpty(message = "{entities.notEmpty}", groups = {SaveGroup.class, EditGroup.class,
        QueryGroup.class, OtherSaveGroup.class, OtherEditGroup.class, OtherQueryGroup.class})
    private List<E> entities;

    /**
     * 构造方法.
     */
    public ValidListDTO() {
        this.entities = new LinkedList<>();
    }

    /**
     * 构造方法.
     *
     * @param entities 对象集合
     */
    public ValidListDTO(List<E> entities) {
        this.entities = entities;
    }

    /**
     * 获取ID列表.
     *
     * @return ID列表
     */
    public List<String> getIdList() {
        if (CollUtil.isEmpty(entities)) {
            return Collections.emptyList();
        }
        Field idField = getIdField(entities.get(0).getClass());
        if (idField == null) {
            return Collections.emptyList();
        }

        List<String> ids = new ArrayList<>(entities.size());
        for (E e : entities) {
            try {
                ids.add(String.valueOf(idField.get(e)));
            } catch (IllegalAccessException ex) {
                log.warn("IllegalAccessException", ex);
            }
        }
        return ids;
    }

    /**
     * id key.
     * </p>
     * 当 idList.size() < 2 时，直接用下划线 _ 拼接即可
     * </p>
     * 当 idList.size() ≥ 2 时，先按 _ 拼接，再对整个字符串做一次 MD5
     *
     * @return String
     */
    public String getIdKey() {
        final List<String> idList = this.getIdList();
        if (CollUtil.isEmpty(idList)) {
            return null;
        }
        // 过滤 null 并转字符串
        String joined = idList.stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .collect(Collectors.joining(StrUtil.UNDERLINE));

        // 长度小于 2 直接返回；否则返回 MD5
        return idList.size() < 2 ? joined : SecureUtil.md5(joined);
    }

    @Override
    public int size() {
        return entities.size();
    }

    @Override
    public boolean isEmpty() {
        return entities.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return entities.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return entities.iterator();
    }

    @Override
    public Object[] toArray() {
        return entities.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return entities.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return entities.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return entities.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return entities.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return entities.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return entities.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return entities.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return entities.retainAll(c);
    }

    @Override
    public void clear() {
        entities.clear();
    }

    @Override
    public E get(int index) {
        return entities.get(index);
    }

    @Override
    public E set(int index, E element) {
        return entities.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        entities.add(index, element);
    }

    @Override
    public E remove(int index) {
        return entities.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return entities.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return entities.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return entities.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return entities.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return entities.subList(fromIndex, toIndex);
    }

    private Field getIdField(Class<?> clazz) {
        return ID_FIELD_CACHE.computeIfAbsent(clazz, k -> {
            Class<?> current = k;
            while (current != null && current != Object.class) {
                try {
                    Field f = current.getDeclaredField(DbConstant.ID);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            log.warn("Missing '{}' field in {}", DbConstant.ID, k.getName());
            return null;
        });
    }

}
