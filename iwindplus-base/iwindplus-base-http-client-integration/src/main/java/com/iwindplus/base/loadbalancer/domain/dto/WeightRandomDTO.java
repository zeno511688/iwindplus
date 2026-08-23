/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.dto;

import lombok.Data;
import lombok.ToString;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 加权随机算法.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2023/10/26 21:30
 */
@ToString
public class WeightRandomDTO<T> {

    /**
     * 泛型集合.
     */
    private final List<T> items = new ArrayList<>(10);

    /**
     * 权重数组.
     */
    private Double[] weights;

    /**
     * 构造方法.
     *
     * @param itemsWithWeight 权重对象
     */
    public WeightRandomDTO(List<ItemWithWeight<T>> itemsWithWeight) {
        this.calWeights(itemsWithWeight);
    }

    private void calWeights(List<ItemWithWeight<T>> itemsWithWeight) {
        items.clear();
        // 计算权重总和
        double originWeightSum = 0;
        for (ItemWithWeight<T> itemWithWeight : itemsWithWeight) {
            double weight = itemWithWeight.getWeight();
            if (weight <= 0) {
                continue;
            }
            items.add(itemWithWeight.getItem());
            if (Double.isInfinite(weight)) {
                weight = 10000.0D;
            }
            if (Double.isNaN(weight)) {
                weight = 1.0D;
            }
            originWeightSum += weight;
        }
        // 计算每个item的实际权重比例
        double[] actualWeightRatios = new double[items.size()];
        int index = 0;
        for (ItemWithWeight<T> itemWithWeight : itemsWithWeight) {
            double weight = itemWithWeight.getWeight();
            if (weight <= 0) {
                continue;
            }
            actualWeightRatios[index++] = weight / originWeightSum;
        }
        // 计算每个item的权重范围
        // 权重范围起始位置
        weights = new Double[items.size()];
        double weightRangeStartPos = 0;
        for (int ii = 0; ii < index; ii++) {
            weights[ii] = weightRangeStartPos + actualWeightRatios[ii];
            weightRangeStartPos += actualWeightRatios[ii];
        }
    }

    /**
     * 基于权重随机算法选择.
     */
    public T choose() {
        if (items.isEmpty()) {
            return null;
        }
        SecureRandom secureRandom = new SecureRandom();
        double random = secureRandom.nextDouble();
        int index = Arrays.binarySearch(weights, random);
        if (index < 0) {
            index = -index - 1;
        } else {
            return items.get(index);
        }
        if (index < weights.length && random < weights[index]) {
            return items.get(index);
        }
        return items.get(0);
    }

    /**
     * 权重类.
     *
     * @param <T> 泛型
     */
    @Data
    public static class ItemWithWeight<T> {

        /**
         * 对象.
         */
        private T item;

        /**
         * 权重.
         */
        private double weight;

        public ItemWithWeight(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }
    }
}
