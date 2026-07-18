/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import com.iwindplus.base.oss.domain.property.OssProperty;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 对象存储业务层基础抽象类.
 *
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractOssBaseServiceImpl extends AbstractBaseConfigServiceImpl<OssProperty> {

    @Autowired
    private OssProperty ossProperty;

    @PostConstruct
    private void init() {
        super.setConfig(ossProperty);
    }
}
