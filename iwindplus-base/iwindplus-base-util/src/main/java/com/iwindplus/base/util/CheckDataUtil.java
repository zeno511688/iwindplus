/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;

/**
 * 校验数据相关工具类.
 *
 * @author zengdegui
 * @since 2026/01/07 22:18
 */
public class CheckDataUtil {

    private CheckDataUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 校验批量操作是否过大.
     *
     * @param dataSize 数据大小
     * @param maxSize  最大值
     */
    public static void checkBatchOperationSize(int dataSize, int maxSize) {
        if (dataSize > maxSize) {
            throw new BizException(BizCodeEnum.BATCH_OPERATION_QUANTITY_TOO_BIG, new Object[]{maxSize});
        }
    }
}
